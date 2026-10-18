package com.aitrade.stock.service.impl;

import com.aitrade.stock.domain.MarketRegimeResult;
import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.handler.StockTaCompleteListener;
import com.aitrade.stock.handler.DayTaResultListener;
import com.aitrade.stock.mapper.StockPickDiagnosticMapper;
import com.aitrade.stock.service.IFinancialRiskFilterService;
import com.aitrade.stock.service.IStockTaService;
import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.factory.StockStrategyFactory;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.stock.strategy.strategies.ChipCalculator;
import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
import com.aitrade.tickflow.enums.TfKlinePeriod;
import com.aitrade.tickflow.repository.AStockKlineRepository;
import com.aitrade.tickflow.repository.TfStockRepository;
import com.aitrade.tickflow.utils.StockFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.aitrade.stock.component.MarketRegimeAnalyzer;

@Service
@Slf4j
public class StockTaServiceImpl implements IStockTaService {
    @Resource
    private AStockKlineRepository aStockKlineRepository;

    @Resource
    private TfStockRepository tfStockRepository;

    @Resource
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private IFinancialRiskFilterService financialRiskFilterService;

    @Autowired
    private MarketRegimeAnalyzer marketRegimeAnalyzer;

    @Autowired
    private StockStrategyFactory strategyFactory;

    @Autowired
    private StockPickDiagnosticMapper diagnosticMapper;

    @Override
    public void startTA(AtomicBoolean isRunning, DayTaResultListener listener) {
        LocalDate date = LocalDate.now();
        startTA(date, isRunning, listener);
    }
    @Override
    public void startTA(LocalDate date, AtomicBoolean isRunning, DayTaResultListener dayTaResultListener) {
        //获取所有股票标识
        List<TfStock> stockList = tfStockRepository.findAll();
        if(stockList == null || stockList.isEmpty()) {
            log.warn("没有任何股票， 返回");
            isRunning.set(false);
            dayTaResultListener.onComplete();
            return;
        }

        //过滤掉退市、ST等不稳定股票
        stockList = stockList
                .stream()
                .filter(stock -> !StockFilter.isRisky(stock.getName(), stock.getCode()))
                .toList();

        //过滤掉基本面不健康，存在假突破风向的股票
        stockList = stockList
                .stream()
                .filter(stock -> !financialRiskFilterService.isRisk(stock.getCode(), date))
                .toList();

        //定基调，判断现在是牛市、熊市，还是震荡市
        //上证指数日K
        List<TfKline> shIndexKlineList = aStockKlineRepository.findBySymbolBefore(TfKlinePeriod.DAY_1, "000001.SH", date, 250);
        MarketRegimeResult marketRegimeResult = marketRegimeAnalyzer.analyze(shIndexKlineList);


        //将任务分发给线程池处理
        int total = stockList.size();
        CountDownLatch latch = new CountDownLatch(total);
        AtomicInteger successCnt = new AtomicInteger(0);
        AtomicInteger failCnt = new AtomicInteger(0);
        Semaphore limiter = new Semaphore(5); // 并发度
        for(final TfStock stock : stockList) {
            scheduledExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        limiter.acquire();
                        doTAOfStock(stock, marketRegimeResult, date, dayTaResultListener, new StockTaCompleteListener() {
                            @Override
                            public void complete(TfStock stock, boolean success) {
                                if(success) {
                                    successCnt.incrementAndGet();
                                } else {
                                    failCnt.incrementAndGet();
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("获取信号量失败", e);
                        Thread.currentThread().interrupt();
                        latch.countDown();
                    }
                }
            });
        }

        //异步等待所有选股任务完成
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                    log.info("所有选股任务完成， 成功 {}， 失败 {}", successCnt.get(), failCnt.get());
                    isRunning.set(false);
                    dayTaResultListener.onComplete();
                } catch (Exception e) {
                    log.error("等待选股任务完成失败", e);
                    isRunning.set(false);
                    dayTaResultListener.onComplete();
                }
            }
        }, scheduledExecutorService);
    }

    /**
     * 对单只股票进行TA分析
     * @param stock 股票标的
     * @param marketRegimeResult 市场基调
     * @param date
     * @param handler
     */
    private void doTAOfStock(TfStock stock, MarketRegimeResult marketRegimeResult, LocalDate date, DayTaResultListener dayTaResultListener, StockTaCompleteListener handler) {
        //根据市场基调，获取对应的股票筛选策略
        StockPickerStrategy strategy = strategyFactory.getStrategy(marketRegimeResult.getRegime());

        //股票日K
        List<TfKline> stockKlineList = aStockKlineRepository.findBySymbolBefore(TfKlinePeriod.DAY_1, stock.getCode(), date, 250);

        // 利用 K 線直接計算籌碼 Context（ 获利盘 profitRatio & 筹码集中度 isChipSinglePeak ）
        StockAnalysisContext context = ChipCalculator.calculateContextFromKlines(stockKlineList);

        StockPickDiagnostic diagnostic = strategy.analyze(stock.getSymbol(), stock.getName(), date, stockKlineList, context);
        if(diagnostic.getPassed().equals(1)) {
            log.info("股票 {}:{} 通过筛选", stock.getSymbol(), stock.getName());
        }

        diagnosticMapper.insertBatch(List.of(diagnostic));

        dayTaResultListener.stockSelectResult(stock, diagnostic);

        handler.complete(stock, true);
    }
}
