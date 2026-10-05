package com.aitrade.stock.service.impl;

import com.aitrade.stock.handler.StockTaHandler;
import com.aitrade.stock.service.IFinancialRiskFilterService;
import com.aitrade.stock.service.IStockTaService;
import com.aitrade.tickflow.domain.TfStock;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public void startTA() {
        LocalDate date = LocalDate.now();
        startTA(date);
    }
    @Override
    public void startTA(LocalDate date) {
        //获取所有股票标识
        List<TfStock> stockList = tfStockRepository.findAll();
        if(stockList == null || stockList.isEmpty()) {
            log.warn("没有任何股票， 返回");
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
                        doTAOfStock(stock, new StockTaHandler() {
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

        //异步等待所有任务完成
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                    log.info("所有任务完成， 成功 {}， 失败 {}", successCnt.get(), failCnt.get());
                } catch (Exception e) {
                    log.error("等待任务完成失败", e);
                }
            }
        }, scheduledExecutorService);
    }

    private void doTAOfStock(TfStock stock, StockTaHandler handler) {

    }
}
