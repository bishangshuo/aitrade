package com.aitrade.stock.service.impl;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;
import com.aitrade.risk.service.ITfRiskReportService;
import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.domain.vo.HolidayGap;
import com.aitrade.stock.handler.ActionMessageListener;
import com.aitrade.stock.handler.DayTaResultListener;
import com.aitrade.stock.mapper.StockPickDiagnosticMapper;
import com.aitrade.stock.service.IBackTestService;
import com.aitrade.stock.service.IStockTaService;
import com.aitrade.stock.service.ITaCalService;
import com.aitrade.tickflow.component.AStockKlineFetcher;
import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class BackTestServiceImpl implements IBackTestService {

    @Autowired
    private IStockTaService stockTaService;

    @Resource
    private StockPickDiagnosticMapper stockPickDiagnosticMapper;

    @Resource
    private AStockKlineFetcher aStockKlineFetcher;

    @Autowired
    private ITaCalService taCalService;

    @Autowired
    private ITfRiskAnnService tfRiskAnnService;

    @Autowired
    private ITfRiskReportService tfRiskReportService;

    @Override
    public SseEmitter backTest(LocalDate fromDate, LocalDate toDate) {
        SseEmitter emitter = new SseEmitter();

        AtomicBoolean cancelled = new AtomicBoolean(false);
        //如果toDate比fromDate还早，就抛出异常
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("截止日比开始日还早，错误");
        }

        doRunTest(cancelled, fromDate, toDate, emitter);

        return emitter;
    }

    private void doRunTest(AtomicBoolean cancelled, LocalDate currentDate, LocalDate toDate, SseEmitter emitter) {
        if(currentDate.isAfter(toDate)) {
            //回测完成
            log.info("回测完成");
            emitter.complete();
            return;
        }

        //currentDate之后的5天，如果遇上中长假，则不买
        HolidayGap holidayGap = taCalService.isLongHolidayAhead(DateUtils.toDate(currentDate));
        if(holidayGap != null && holidayGap.isWillBeHoliday()) {
            //遇上中长假，则不买；并跳到假后第一个交易日
            log.warn("遇上中长假，则不买；并跳到假后第一个交易日 {}", holidayGap);
            Date nextTradeDate = holidayGap.getTradeDateAfterHoliday();
            LocalDate nextTradeLocalDate = nextTradeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            doRunTest(cancelled, nextTradeLocalDate, toDate, emitter);
            return;
        }

        AtomicBoolean isRunning = new AtomicBoolean(false);
        stockTaService.startTA(currentDate, isRunning, new DayTaResultListener() {
            @Override
            public void onComplete() {
                //回测买入、持仓、卖出
                takeAction(cancelled, currentDate, emitter, new ActionMessageListener() {

                    @Override
                    public void onComplete() {
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(cancelled, nextDate, toDate, emitter);
                    }

                    @Override
                    public void onFail(String message) {
                        log.warn("当日没有选出符合条件的股票 {}", currentDate);
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(cancelled, nextDate, toDate, emitter);
                    }
                });
            }

            @Override
            public void stockSelectResult(TfStock stock, StockPickDiagnostic diagnostic) {
                //发送个股选股结果的sse消息
            }
        });
    }

    //模拟买入、持仓、卖出
    private void takeAction(AtomicBoolean cancelled, LocalDate currentDate, SseEmitter emitter, ActionMessageListener actionMessageListener) {
        //拿取ta选股结果为passed的列表
        StockPickDiagnostic query = new StockPickDiagnostic();
        query.setTradeDate(currentDate);
        query.setPassed(1);
        //备选50个
        List<StockPickDiagnostic> passedStocks = stockPickDiagnosticMapper.selectTopScore(query, 50);
        if(passedStocks == null || passedStocks.isEmpty()) {
            log.info("没有找到符合条件的股票");
            actionMessageListener.onFail("没有找到符合条件的股票");
            return;
        }
        //获取风险公告的时间：currentDate 到T+1日的9:15:00之前
        Date nextTradeDate = taCalService.getNextTradeDate(DateUtils.toDate(currentDate));
        //固定nextTradeDate为YYYY-MM-DD 09:15:00格式
        nextTradeDate = DateUtils.parseDate(DateFormatUtils.format(nextTradeDate, "yyyy-MM-dd 09:15:00"));
        //选排前面的10个个股进行买入
        AtomicInteger bought = new AtomicInteger();
        processPassedStocks(passedStocks, bought, cancelled, currentDate, nextTradeDate, emitter)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.error("批量处理异常", ex);
                        actionMessageListener.onFail("批量处理异常");
                    }
                    else {
                        log.info("处理完毕，实际买入 {} 个", bought.get());
                        actionMessageListener.onComplete();
                    }
                });
    }

    /** 入口：返回的 future 在所有股票处理完毕（或买够 10 个）后完成 */
    public CompletableFuture<Void> processPassedStocks(
            List<StockPickDiagnostic> passedStocks,
            AtomicInteger bought,          // 注意：需是 AtomicInteger
            AtomicBoolean cancelled,
            LocalDate currentDate,
            Date nextTradeDate,
            SseEmitter emitter)
    {
        return processFrom(passedStocks, 0, bought, cancelled, currentDate, nextTradeDate, emitter);
    }

    private CompletableFuture<Void> processFrom(
            List<StockPickDiagnostic> passedStocks, int index,
            AtomicInteger bought, AtomicBoolean cancelled,
            LocalDate currentDate, Date nextTradeDate, SseEmitter emitter) {

        // 终止条件：买够 10 个 / 已取消 / 遍历完
        if (bought.get() >= 10 || cancelled.get() || index >= passedStocks.size()) {
            return CompletableFuture.completedFuture(null);
        }

        StockPickDiagnostic stock = passedStocks.get(index);

        // 过滤黑天鹅：下载公司公告
        List<TfRiskAnn> riskAnn = tfRiskAnnService.getRiskAnnFromEastMoneyBackward(
                stock.getSymbol(), nextTradeDate, 30, 3);

        CompletableFuture<Void> step;
        if (riskAnn != null && !riskAnn.isEmpty()) {
            // 有风险公告 -> AI 深度复核
            step = aiTradeReportAsync(cancelled, stock, riskAnn)
                    .exceptionally(ex -> {
                        // 复核失败：记日志，视为不通过，继续下一个（与原逻辑一致）
                        log.error("风险公告深度复核失败: {}", stock, ex);
                        return false;
                    })
                    .thenAccept(passed -> {
                        if (Boolean.TRUE.equals(passed)) {
                            log.info("风险公告深度复核通过: {}", stock);
                            begeinTrade(cancelled, stock, currentDate, emitter);
                            bought.getAndIncrement();
                        } else {
                            log.info("风险公告深度复核未通过: {}", stock);
                        }
                    });
        } else {
            // 无风险公告 -> 直接买入
            begeinTrade(cancelled, stock, currentDate, emitter);

            bought.getAndIncrement();
            step = CompletableFuture.completedFuture(null);
        }

        // thenCompose 保证严格串行：当前股票处理完才处理下一个
        return step.thenCompose(v ->
                processFrom(passedStocks, index + 1, bought, cancelled, currentDate, nextTradeDate, emitter));
    }

    /** 把回调式 aiTradeReport 包装成 CompletableFuture<Boolean> */
    private CompletableFuture<Boolean> aiTradeReportAsync(
            AtomicBoolean cancelled, StockPickDiagnostic stockPickDiagnostic, List<TfRiskAnn> riskAnn) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        tfRiskReportService.aiTradeReport(cancelled, stockPickDiagnostic, riskAnn,
                content -> true,
                riskReport -> {

                    future.complete(riskReport.getPassed());
                    return true;
                },
                () -> future.completeExceptionally(
                        new IllegalStateException("风险公告深度复核失败: " + stockPickDiagnostic))
        );
        return future;
    }

    private void begeinTrade(AtomicBoolean cancelled, StockPickDiagnostic stockPickDiagnostic, LocalDate currentDate, SseEmitter emitter) {

        //获取T+1日到T+5日的stock的分时k线
        TfStock stock = new TfStock();
        stock.setSymbol(stockPickDiagnostic.getSymbol());
        stock.setName(stockPickDiagnostic.getStockName());
        List<TfKline> klines = aStockKlineFetcher.getKlineSingle(stock, currentDate, 5, "1m");
        //买入
        buyStock(currentDate, emitter, stockPickDiagnostic, klines);
        //发送买入信号
        //卖出
        sellStock(currentDate, emitter, stockPickDiagnostic, klines);
        //发送卖出信号
    }

    /**
     * 买入回测
     * @param currentDate
     * @param emitter
     * @param stockPickDiagnostic
     * @param klines
     */
    private void buyStock(LocalDate currentDate, SseEmitter emitter, StockPickDiagnostic stockPickDiagnostic, List<TfKline> klines) {
        if(klines == null || klines.isEmpty()) {
            log.warn("该股票自{}的第二个交易日起的5天内没有K线数据，可能停牌了, 不买入", currentDate.toString());
        }
    }

    private void sellStock(LocalDate currentDate, SseEmitter emitter, StockPickDiagnostic stockPickDiagnostic, List<TfKline> klines) {

    }
}
