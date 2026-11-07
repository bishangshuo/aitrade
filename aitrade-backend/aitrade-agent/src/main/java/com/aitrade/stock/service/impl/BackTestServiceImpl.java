package com.aitrade.stock.service.impl;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;
import com.aitrade.risk.service.ITfRiskReportService;
import com.aitrade.stock.domain.*;
import com.aitrade.stock.domain.vo.HolidayGap;
import com.aitrade.stock.enums.BackTestMsg;
import com.aitrade.stock.enums.FlowType;
import com.aitrade.stock.enums.SellReason;
import com.aitrade.stock.enums.TradeType;
import com.aitrade.stock.handler.ActionMessageListener;
import com.aitrade.stock.handler.DayTaResultListener;
import com.aitrade.stock.mapper.*;
import com.aitrade.stock.service.*;
import com.aitrade.tickflow.component.AStockKlineFetcher;
import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
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

    @Autowired
    private TfBacktestRecordMapper tfBacktestRecordMapper;

    @Resource
    private ScheduledExecutorService sseExecutor;

    @Autowired
    private TfAccountMapper tfAccountMapper;

    @Autowired
    private ICommissionTaxService commissionTaxService;

    @Autowired
    private TfTradeExecutionMapper tfTradeExecutionMapper;

    @Autowired
    private TfPositionMapper tfPositionMapper;

    @Autowired
    private TfFundFlowMapper tfFundFlowMapper;

    @Override
    public SseEmitter backTest(TfBacktestRecord record) {
        TfBacktestRecord tfBacktestRecord = tfBacktestRecordMapper.selectTfBacktestRecordById(record.getId());
        if (tfBacktestRecord == null) {
            throw new IllegalArgumentException("回测记录不存在");
        }

        SseEmitter emitter = new SseEmitter();

        AtomicBoolean cancelled = new AtomicBoolean(false);

        // SSE 发送锁，保证多线程环境下的线程安全
        final Object sseLock = new Object();

        // ===== 心跳 =====
        ScheduledFuture<?> heartbeatFuture = sseExecutor.scheduleAtFixedRate(() -> {
            if (cancelled.get()) return; // 已取消则不发送心跳
            try {
                synchronized (sseLock) {
                    emitter.send(SseEmitter.event()
                            .name(BackTestMsg.HEART_BEAT.getCode())
                            .data("ping"));
                }
            } catch (Exception e) {
                log.warn("心跳发送失败，连接可能已断开: {}", e.getMessage());
            }
        }, 16, 16, TimeUnit.SECONDS);

        Future<?> businessFuture = sseExecutor.submit(() -> {
            try {
                LocalDate currentDate = DateUtils.toLocalDate(tfBacktestRecord.getFromDate());
                LocalDate toDate = DateUtils.toLocalDate(tfBacktestRecord.getToDate());
                doRunTest(tfBacktestRecord, cancelled, currentDate, toDate, sseLock, emitter);
            } catch (Exception e) {
                log.error("回测执行异常", e);
                completeWithFlush(emitter, "系统异常: " + e.getMessage(), false, sseLock);
            }
        });

        // ===== 生命周期清理 =====
        Runnable cleanup = () -> {
            cancelled.set(true); // 【关键】：客户端断开时，标记任务为已取消
            heartbeatFuture.cancel(true);
            businessFuture.cancel(true);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    private void doRunTest(TfBacktestRecord backtestRecord, AtomicBoolean cancelled, LocalDate currentDate, LocalDate toDate, Object sseLock, SseEmitter emitter) {
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
            doRunTest(backtestRecord, cancelled, nextTradeLocalDate, toDate, sseLock, emitter);
            return;
        }

        AtomicBoolean isRunning = new AtomicBoolean(false);
        stockTaService.startTA(backtestRecord, currentDate, isRunning, new DayTaResultListener() {
            @Override
            public void onComplete() {
                //回测买入、持仓、卖出
                takeAction(backtestRecord, cancelled, currentDate, emitter, new ActionMessageListener() {

                    @Override
                    public void onComplete() {
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(backtestRecord, cancelled, nextDate, toDate, sseLock, emitter);
                    }

                    @Override
                    public void onFail(String message) {
                        log.warn("当日没有选出符合条件的股票 {}", currentDate);
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(backtestRecord, cancelled, nextDate, toDate, sseLock, emitter);
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
    private void takeAction(TfBacktestRecord backtestRecord, AtomicBoolean cancelled, LocalDate currentDate, SseEmitter emitter, ActionMessageListener actionMessageListener) {
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
        processPassedStocks(backtestRecord, passedStocks, bought, cancelled, currentDate, nextTradeDate, emitter)
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
            TfBacktestRecord backtestRecord,
            List<StockPickDiagnostic> passedStocks,
            AtomicInteger bought,          // 注意：需是 AtomicInteger
            AtomicBoolean cancelled,
            LocalDate currentDate,
            Date nextTradeDate,
            SseEmitter emitter)
    {
        return processFrom(backtestRecord, passedStocks, 0, bought, cancelled, currentDate, nextTradeDate, emitter);
    }

    private CompletableFuture<Void> processFrom(
            TfBacktestRecord backtestRecord,
            List<StockPickDiagnostic> passedStocks,
            int index,
            AtomicInteger bought,
            AtomicBoolean cancelled,
            LocalDate currentDate,
            Date nextTradeDate,
            SseEmitter emitter
    ) {

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
                            begeinTrade(backtestRecord, cancelled, stock, currentDate, emitter);
                            bought.getAndIncrement();
                        } else {
                            log.info("风险公告深度复核未通过: {}", stock);
                        }
                    });
        } else {
            // 无风险公告 -> 直接买入
            begeinTrade(backtestRecord, cancelled, stock, currentDate, emitter);

            bought.getAndIncrement();
            step = CompletableFuture.completedFuture(null);
        }

        // thenCompose 保证严格串行：当前股票处理完才处理下一个
        return step.thenCompose(v ->
                processFrom(backtestRecord, passedStocks, index + 1, bought, cancelled, currentDate, nextTradeDate, emitter));
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

    private void begeinTrade(TfBacktestRecord backtestRecord, AtomicBoolean cancelled, StockPickDiagnostic stockPickDiagnostic, LocalDate currentDate, SseEmitter emitter) {

        //获取T+1日到T+5日的stock的分时k线
        TfStock stock = new TfStock();
        stock.setSymbol(stockPickDiagnostic.getSymbol());
        stock.setName(stockPickDiagnostic.getStockName());
        List<TfKline> klines = aStockKlineFetcher.getKlineSingle(stock, currentDate, 5, "1m");
        //买入
        TfTradeExecution execution = buyStock(backtestRecord, currentDate, emitter, stockPickDiagnostic, klines);
        //发送买入信号
        //卖出
        if(execution != null) {
            sellStock(execution, backtestRecord, currentDate, emitter, stockPickDiagnostic, klines);
        }
        //发送卖出信号
    }

    /**
     * 买入回测
     * @param currentDate
     * @param emitter
     * @param stockPickDiagnostic
     * @param klines
     */
    private TfTradeExecution buyStock(TfBacktestRecord backtestRecord, LocalDate currentDate, SseEmitter emitter, StockPickDiagnostic stockPickDiagnostic, List<TfKline> klines) {
        if(klines == null || klines.isEmpty()) {
            log.warn("该股票自{}的第二个交易日起的5天内没有K线数据，可能停牌了, 不买入", currentDate.toString());
                return null;
        }
        //T+1日开盘价,去开盘时间9:30
        LocalDate openTime = LocalDate.of(9, 30, 0);
        BigDecimal openPrice = BigDecimal.ZERO;
        LocalDate tradeTime = null;
        boolean found = false;
        for(TfKline kline : klines) {
            //k线数据中第一个T+1日的9:30开盘价
            LocalDate klineTradeDate = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
            if(klineTradeDate.isAfter(currentDate)  && !klineTradeDate.isBefore(openTime)) {
                openPrice = kline.getOpen();
                tradeTime = klineTradeDate;
                found = true;
                break;
            }
        }
        if(!found) {
            log.warn("该股票自{}的第二个交易日起的5天内没有K线数据，可能停牌了, 不买入", currentDate.toString());
            return null;
        }
        //如果开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，则不买入
        BigDecimal threshold = backtestRecord.getCnfTrade().getT1HighOpenRatio();
        BigDecimal previousClosePrice = stockPickDiagnostic.getClosePrice();
        if (openPrice.compareTo(previousClosePrice) > 0) {
            if(openPrice.compareTo(previousClosePrice.multiply(threshold.add(BigDecimal.ONE))) > 0) {
                log.warn("该股票的开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，不买入, {}", stockPickDiagnostic.getStockName());
                return null;
            }
        }
        //买入，开仓
        String accountNo = "100001";
        // 1. 从账户里减总资金的1/10用于开仓
        TfAccount account = tfAccountMapper.selectTfAccountByNo(accountNo);
        if (account == null) {
            log.error("账户不存在: {}", accountNo);
            return null;
        }
        //持仓市值
        BigDecimal marketValue = account.getMarketValue();
        //可用资金
        BigDecimal availableCash = account.getAvailableCash();

        //买入金额
        BigDecimal buyAmount = (availableCash.add(marketValue)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);

        //可用金额要比 买入金额+手续费 大， 估算一次10笔，每笔10万，手续费预留100000*10*0.00025 = 250，取上线500
        BigDecimal reserveFee = BigDecimal.valueOf(500);
        if(buyAmount.compareTo(availableCash.subtract(reserveFee)) > 0) {
            log.warn("可用资金不足，不买入: {}", stockPickDiagnostic.getStockName());
            return null;
        }

        //买入手数，以手为单位，一手100股
        long buyQuantity = buyAmount.divide(openPrice, 0, RoundingMode.HALF_UP).longValue() / 100;
        long buyVolume = buyQuantity * 100;
        //买入金额
        BigDecimal tradeAmount = openPrice.multiply(BigDecimal.valueOf(buyVolume));

        //交易佣金
        BigDecimal tradeCommission = commissionTaxService.getCommission(tradeAmount);
        //过户费
        BigDecimal transferFee = commissionTaxService.getTransferFee(tradeAmount);
        //总费用
        BigDecimal totalFee = tradeCommission.add(transferFee);

        //生成一个订单编号
        String orderId = UUID.randomUUID().toString();
        log.info("买入股票: {}", stockPickDiagnostic.getStockName());
        TfTradeExecution execution = new TfTradeExecution();
        execution.setAccountNo(accountNo);
        execution.setOrderId(orderId);
        execution.setSymbol(stockPickDiagnostic.getSymbol());
        execution.setStockName(stockPickDiagnostic.getStockName());
        execution.setTradeSide(TradeType.BUY.getValue());
        execution.setTradePrice(openPrice);
        execution.setTradeVolume(buyVolume);
        execution.setTradeAmount(tradeAmount);
        execution.setCommission(totalFee);
        execution.setTax(BigDecimal.ZERO);
        execution.setTradeTime(DateUtils.toDate(tradeTime));
        execution.setCreateTime(new Date());
        tfTradeExecutionMapper.insertTfTradeExecution(execution);

        //持仓
        TfPosition position = new TfPosition();
        position.setAccountNo(accountNo);
        position.setSymbol(stockPickDiagnostic.getSymbol());
        position.setStockName(stockPickDiagnostic.getStockName());
        position.setTotalVolume(buyVolume);
        //可用股數（T+1：當天買入的為 0，次日解凍變為總股數
        position.setAvailableVolume(0L);
        position.setCostPrice(openPrice);
        position.setCurrentPrice(openPrice);
        position.setMarketValue(tradeAmount);
        position.setOpenDate(DateUtils.toDate(tradeTime));
        position.setUpdateTime(DateUtils.toDate(tradeTime));
        tfPositionMapper.insertTfPosition(position);

        //更新账户
        //可用余额减少tradeAmount+totalFee
        account.setAvailableCash(account.getAvailableCash().subtract(tradeAmount).subtract(totalFee));
        //持仓市值增加tradeAmount
        account.setMarketValue(account.getMarketValue().add(tradeAmount));
        //总资产减少totalFee
        account.setTotalAssets(account.getTotalAssets().subtract(totalFee));
        account.setUpdateTime(DateUtils.toDate(tradeTime));
        tfAccountMapper.updateTfAccount(account);

        //添加账单记录
        //1买入扣款
        TfFundFlow buyFlow = new TfFundFlow();
        buyFlow.setAccountNo(accountNo);
        buyFlow.setTradeDate(DateUtils.toDate(tradeTime));
        buyFlow.setBizType(FlowType.BUY.getCode());
        buyFlow.setAmount(tradeAmount);
        buyFlow.setRelatedOrderId(orderId);
        tfFundFlowMapper.insertTfFundFlow(buyFlow);
        //2佣金支出
        TfFundFlow commissionFlow = new TfFundFlow();
        commissionFlow.setAccountNo(accountNo);
        commissionFlow.setTradeDate(DateUtils.toDate(tradeTime));
        commissionFlow.setBizType(FlowType.COMMISSION.getCode());
        commissionFlow.setAmount(tradeCommission);
        commissionFlow.setRelatedOrderId(orderId);
        tfFundFlowMapper.insertTfFundFlow(commissionFlow);
        //3过户费支出
        TfFundFlow transferFeeFlow = new TfFundFlow();
        transferFeeFlow.setAccountNo(accountNo);
        transferFeeFlow.setTradeDate(DateUtils.toDate(tradeTime));
        transferFeeFlow.setBizType(FlowType.TRANSFERFEE.getCode());
        transferFeeFlow.setAmount(transferFee);
        transferFeeFlow.setRelatedOrderId(orderId);
        tfFundFlowMapper.insertTfFundFlow(transferFeeFlow);

        return execution;
    }

    private void sellStock(TfTradeExecution buyExecution, TfBacktestRecord backtestRecord, LocalDate currentDate, SseEmitter emitter, StockPickDiagnostic stockPickDiagnostic, List<TfKline> klines) {
        //持仓5天，遍历klines，发现价格到达止盈价或止损价，则卖出
        for(TfKline kline : klines) {
            //在T+2日开盘价时间后，如果价格到达止盈价或止损价，则卖出
            LocalDate t2Date = currentDate.plusDays(2);
            LocalDate klineTradeDate = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
            if(klineTradeDate.isAfter(t2Date)) {
                BigDecimal open = kline.getOpen();
                //策略算出的目标止盈价和止损价
                BigDecimal targetPrice = stockPickDiagnostic.getTargetPrice5d();
                BigDecimal lossPrice = stockPickDiagnostic.getStopLossPrice();
                //保守模式下，止盈价为targetPrice*0.8，止损价为targetPrice*1.1
                targetPrice = targetPrice.multiply(BigDecimal.valueOf(0.8));
                lossPrice = targetPrice.multiply(BigDecimal.valueOf(1.1));
                //到了T+5日的上午11点，不论价格如何都要卖出
                LocalDate t5Date = currentDate.plusDays(5);
                LocalDate t5Date11am = LocalDate.of(t5Date.getYear(), t5Date.getMonth(), t5Date.getDayOfMonth()).atTime(11, 0, 0).toLocalDate();
                if(klineTradeDate.isAfter(t5Date11am)) {
                    //卖出
                    sellExecution(buyExecution, backtestRecord, currentDate, emitter, stockPickDiagnostic, kline, SellReason.TIMEOUT);
                    break;
                }

                //如果价格到达止盈价，则卖出
                if(open.compareTo(targetPrice) >= 0) {
                    //卖出
                    sellExecution(buyExecution, backtestRecord, currentDate, emitter, stockPickDiagnostic, kline, SellReason.PROFIT);
                    break;
                }

                //如果价格到达止损价，则卖出
                if(open.compareTo(lossPrice) <= 0) {
                    sellExecution(buyExecution, backtestRecord, currentDate, emitter, stockPickDiagnostic, kline, SellReason.LOSS);
                    break;
                }
            }
        }
    }

    //卖出股票
    private void sellExecution(
            TfTradeExecution buyExecution,
            TfBacktestRecord backtestRecord,
            LocalDate currentDate,
            SseEmitter emitter,
            StockPickDiagnostic stockPickDiagnostic,
            TfKline kline,
            SellReason reason
    ) {
        log.info("卖出股票: {}", stockPickDiagnostic.getStockName());

        LocalDate klineTradeDate = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();

        //全部卖出
        long sellVolume = buyExecution.getTradeVolume().intValue();
        //卖出价格
        BigDecimal sellPrice = kline.getOpen();
        //卖出金额
        BigDecimal sellAmount = sellPrice.multiply(BigDecimal.valueOf(sellVolume));

        //交易佣金/手续费
        BigDecimal sellCommission = commissionTaxService.getCommission(sellAmount);
        //过户费
        BigDecimal transferFee = commissionTaxService.getTransferFee(sellAmount);
        //总费用
        BigDecimal totalFee = sellCommission.add(transferFee);
        //印花税
        BigDecimal stampTax = commissionTaxService.getTax(sellAmount);

        //本次卖出回笼资金
        BigDecimal returnAmount = sellAmount.subtract(totalFee).subtract(stampTax);

        //盈利=卖出金额-买入金额-总费用,有可能为负
        BigDecimal profit = sellAmount.subtract(buyExecution.getTradeAmount()).subtract(totalFee).subtract(stampTax).subtract(buyExecution.getCommission());

        //创建卖出执行记录
        TfTradeExecution sellExecution = new TfTradeExecution();
        sellExecution.setAccountNo(buyExecution.getAccountNo());
        //方便查询，买卖同一个订单编号
        sellExecution.setOrderId(buyExecution.getOrderId());
        sellExecution.setSymbol(buyExecution.getSymbol());
        sellExecution.setStockName(buyExecution.getStockName());
        sellExecution.setTradeSide(TradeType.SELL.getValue());
        sellExecution.setTradeReason(reason.getCode());
        //以k线的开盘价为卖出价格
        sellExecution.setTradePrice(sellPrice);
        sellExecution.setTradeVolume(sellVolume);
        sellExecution.setTradeAmount(sellAmount);
        sellExecution.setCommission(totalFee);
        sellExecution.setTax(stampTax);
        sellExecution.setRealizedPnl(profit);
        sellExecution.setTradeTime(DateUtils.toDate(klineTradeDate));
        sellExecution.setCreateTime(new Date());
        tfTradeExecutionMapper.insertTfTradeExecution(sellExecution);

        //从持仓中平仓
        TfPosition queryPosition = new TfPosition();
        queryPosition.setAccountNo(buyExecution.getAccountNo());
        queryPosition.setSymbol(buyExecution.getSymbol());
        List<TfPosition> positionList = tfPositionMapper.selectTfPositionList(queryPosition);
        if(positionList != null && !positionList.isEmpty()) {
            tfAccountMapper.deleteTfAccountByIds(new Long[]{positionList.get(0).getId()});
        }

        //更新账户
        TfAccount account = tfAccountMapper.selectTfAccountByNo(buyExecution.getAccountNo());
        //可用金额+回笼资金
        account.setAvailableCash(account.getAvailableCash().add(returnAmount));
        //持仓市值减少sellAmount
        account.setMarketValue(account.getMarketValue().subtract(sellAmount));
        //更新总资产
        account.setTotalAssets(account.getAvailableCash().add(account.getMarketValue()));
        //累计盈亏金额
        account.setTotalPnl(account.getTotalPnl().add(profit));
        //累计盈亏比例
        account.setTotalPnlPct(account.getTotalPnl().divide(account.getInitialCapital(), 4, RoundingMode.HALF_UP));
        account.setUpdateTime(DateUtils.toDate(klineTradeDate));
        tfAccountMapper.updateTfAccount(account);
    }

    private void sendSSEMessage(String eventName, TfBacktestRecord record, String message, SseEmitter emitter, Object sseLock) {
        try {
            JSONObject json = new JSONObject();
            json.put("record", record);
            json.put("message", message);

            synchronized (sseLock) {
                emitter.send(SseEmitter.event().name(eventName).data(json));
            }
        } catch (Exception e) {
            log.error("发送SSE消息失败: {}", e.getMessage());
        }
    }

    private void completeWithFlush(SseEmitter emitter, String message, boolean success, Object sseLock) {
        try {
            if (success) {
                emitter.complete();
            } else {
                synchronized (sseLock) {
                    emitter.send(SseEmitter.event().name("error").data(message));
                }
                emitter.completeWithError(new RuntimeException(message));
            }
        } catch (Exception e) {
            log.warn("关闭SSE连接时异常: {}", e.getMessage());
        }
    }
}
