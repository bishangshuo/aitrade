package com.aitrade.stock.service.impl;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.risk.domain.RiskReport;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;
import com.aitrade.risk.service.ITfRiskReportService;
import com.aitrade.stock.domain.*;
import com.aitrade.stock.domain.vo.HolidayGap;
import com.aitrade.stock.enums.SellReason;
import com.aitrade.stock.enums.TradeType;
import com.aitrade.stock.mapper.*;
import com.aitrade.stock.service.*;
import com.aitrade.tickflow.component.AStockKlineFetcher;
import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BacktestRunServiceImpl implements IBacktestRunService {

    @Autowired
    private ITaCalService taCalService;

    @Autowired
    private ITfAccountDailyService tfAccountDailyService;

    @Autowired
    private TfAccountMapper tfAccountMapper;

    @Autowired
    private TfPositionMapper tfPositionMapper;

    @Autowired
    private TfPositionDailyMapper tfPositionDailyMapper;

    @Resource
    private StockPickDiagnosticMapper stockPickDiagnosticMapper;

    @Autowired
    private ITfRiskAnnService tfRiskAnnService;

    @Autowired
    private ITfRiskReportService tfRiskReportService;

    @Autowired
    private ITfPositionService tfPositionService;

    @Resource
    private AStockKlineFetcher aStockKlineFetcher;

    @Autowired
    private ICommissionTaxService commissionTaxService;

    @Autowired
    private TfTradeExecutionMapper tfTradeExecutionMapper;

    @Autowired
    private TfFundFlowMapper tfFundFlowMapper;

    @Override
    public SseEmitter runBacktest(TfBacktestRecord backtestRecord) {
        // TODO: Implement the logic to run backtest



        return null;
    }

    private void doRunBacktest(TfBacktestRecord backtestRecord, SseEmitter emitter) {
        //初始化账户
        initAccount(backtestRecord);

        //取交易日志列表
        TaCal queryCal = new TaCal();
        queryCal.setBeginDate(backtestRecord.getFromDate());
        queryCal.setEndDate(backtestRecord.getToDate());
        List<TaCal> taCalList = taCalService.selectTaCalList(queryCal);

        for(TaCal cal : taCalList) {
            // 如果是交易日，才执行买卖和选股
            if(cal.getIsOpen().equals(1)) {
                //计算未来是否有长假, 且预留1天用于节前平仓
                HolidayGap holidayGap = taCalService.isLongHolidayAhead(cal.getCalDate(), backtestRecord.getCnfTrade().getHoldDays().intValue());
                /**
                 * 1. 执行买入
                 */
                if(holidayGap == null) {
                    buyLogic(backtestRecord, cal, emitter);
                }

                /**
                 * 2. 执行卖出
                 * 只要是交易日，并满足条件，都要卖出
                 */
                sellLogic(backtestRecord, cal, emitter);

                //更新账户和持仓日历，马上可以看到效果
                updateAccountAndPositionDaily(backtestRecord, cal, emitter);

                /**
                 * 3. 执行选股
                 */
                if(holidayGap == null) {
                    pickLogic(backtestRecord, cal, emitter);
                }
            }
            //只更新账户日历 和 持仓日历
            else {
                updateAccountAndPositionDaily(backtestRecord, cal, emitter);
            }
        }
    }

    /**
     * 初始化账户
     * @param backtestRecord
     */
    private void initAccount(TfBacktestRecord backtestRecord) {
        TfAccount account = tfAccountMapper.selectTfAccountByNo(backtestRecord.getAccountNo());
        if(account == null) {
            account = new TfAccount();
            account.setAccountNo(backtestRecord.getAccountNo());
            account.setInitialCapital(backtestRecord.getInitialCash());
            account.setAvailableCash(backtestRecord.getInitialCash());
            account.setFrozenCash(BigDecimal.ZERO);
            account.setMarketValue(BigDecimal.ZERO);
            account.setTotalAssets(backtestRecord.getInitialCash());
            account.setCreateTime(new Date());
            tfAccountMapper.insertTfAccount(account);
        } else {
            account.setInitialCapital(backtestRecord.getInitialCash());
            account.setAvailableCash(backtestRecord.getInitialCash());
            account.setFrozenCash(BigDecimal.ZERO);
            account.setMarketValue(BigDecimal.ZERO);
            account.setTotalAssets(backtestRecord.getInitialCash());
            account.setTotalPnl(BigDecimal.ZERO);
            account.setTotalPnlPct(BigDecimal.ZERO);
            account.setUpdateTime(new Date());
            tfAccountMapper.updateTfAccount(account);
        }
    }

    /**
     * 更新账户日历和持仓日历
     */
    private void updateAccountAndPositionDaily(TfBacktestRecord backtestRecord, TaCal cal, SseEmitter sseEmitter) {

        //创建账户日历
        TfAccount account = tfAccountMapper.selectTfAccountByNo(backtestRecord.getAccountNo());
        TfAccountDaily accountDaily = new TfAccountDaily();
        accountDaily.setAccountNo(backtestRecord.getAccountNo());
        accountDaily.setBacktestId(backtestRecord.getId());
        accountDaily.setCalenderDate(cal.getCalDate());
        accountDaily.setAvailableCash(account.getAvailableCash());
        accountDaily.setFrozenCash(account.getFrozenCash());
        accountDaily.setMarketValue(account.getMarketValue());
        accountDaily.setTotalAssets(account.getTotalAssets());
        accountDaily.setTotalPnl(account.getTotalPnl());
        accountDaily.setTotalPnlPct(account.getTotalPnlPct());
        accountDaily.setCreateTime(new Date());
        accountDaily.setUpdateTime(null);
        tfAccountDailyService.insertTfAccountDaily(accountDaily);

        //创建持仓日历
        TfPosition queryPosition = new TfPosition();
        queryPosition.setAccountNo(backtestRecord.getAccountNo());
        List<TfPosition> positionList = tfPositionMapper.selectTfPositionList(queryPosition);
        for(TfPosition position : positionList) {
            TfPositionDaily positionDaily = new TfPositionDaily();
            positionDaily.setAccountNo(backtestRecord.getAccountNo());
            positionDaily.setBacktestId(backtestRecord.getId());
            positionDaily.setPositionDate(cal.getCalDate());
            positionDaily.setSymbol(position.getSymbol());
            positionDaily.setStockName(position.getStockName());
            positionDaily.setTotalVolume(position.getTotalVolume());
            positionDaily.setAvailableVolume(position.getAvailableVolume());
            positionDaily.setCostPrice(position.getCostPrice());
            positionDaily.setCurrentPrice(position.getCurrentPrice());
            positionDaily.setMarketValue(position.getMarketValue());
            positionDaily.setUnrealizedPnl(position.getUnrealizedPnl());
            positionDaily.setUnrealizedPnlPct(position.getUnrealizedPnlPct());
            positionDaily.setCreateTime(new Date());
            positionDaily.setUpdateTime(null);
            tfPositionDailyMapper.insertTfPositionDaily(positionDaily);
        }
    }

    /**
     * 买入逻辑
     */
    private void buyLogic(TfBacktestRecord backtestRecord, TaCal cal, SseEmitter sseEmitter) {
        //从前一天选中的备选股票列表中买入
        //1 获取备选列表
        //前一个交易日
        Date prevTradeDate = taCalService.getPrevTradeDate(cal.getCalDate());
        if(prevTradeDate == null) {
            //前一个交易日不存在
            log.warn("前一个交易日不存在 {}", cal.getCalDate());
            return;
        }
        StockPickDiagnostic query = new StockPickDiagnostic();
        query.setBacktestId(backtestRecord.getId());
        query.setTradeDate(DateUtils.toLocalDate(prevTradeDate));
        query.setPassed(1);
        query.setStatus(0);
        //备选50个
        List<StockPickDiagnostic> passedStocks = stockPickDiagnosticMapper.selectTopScore(query, 50);

        if(passedStocks == null || passedStocks.isEmpty()) {
            log.warn("没有找到符合条件的备选股票 {} ", cal.getCalDate());
            return;
        }

        // 2 估算每单买入的最大金额，每单金额大概占总资产的1/N，用于摊薄风险
        TfAccount account = tfAccountMapper.selectTfAccountByNo(backtestRecord.getAccountNo());
        Integer diversification = backtestRecord.getCnfTrade().getDiversification();
        BigDecimal maxSingleAmount = account.getTotalAssets().divide(BigDecimal.valueOf(diversification), 2, RoundingMode.HALF_UP);
        //同时也要定义一个最小买入金额，防止因为资产不足而无法买入
        BigDecimal minSingleAmount = BigDecimal.valueOf(2000);
        //遍历
        int bought = 0;
        List<Long> allPickIds = passedStocks.stream().map(StockPickDiagnostic::getId).collect(Collectors.toList());
        List<Long> pickIds = new ArrayList<>();
        for(StockPickDiagnostic passedStock : passedStocks) {
            if(bought > diversification.intValue()) {
                log.warn("已购买10只股票，不再购买 {}", cal.getCalDate());
                //未处理的pickIds
                List<Long> unprocessedPickIds = allPickIds.stream().filter(id -> !pickIds.contains(id)).collect(Collectors.toList());
                if(unprocessedPickIds != null && !unprocessedPickIds.isEmpty()) {
                    stockPickDiagnosticMapper.updateStatusByIds(unprocessedPickIds.toArray(new Long[0]), 2);
                }
                return;
            }

            //获取账户信息，用于判断可用资金是否足够, 触发等级最高
            TfAccount accountTemp = tfAccountMapper.selectTfAccountByNo(backtestRecord.getAccountNo());
            BigDecimal availableCash = accountTemp.getAvailableCash();
            //并且要预留10%的资金用于手续费，否则，在实盘里是无法成交的。也就失去回测的意义。
            availableCash = availableCash.multiply(BigDecimal.valueOf(0.9));
            BigDecimal buyAmount = availableCash.compareTo(maxSingleAmount) > 0 ?
                    maxSingleAmount : availableCash.compareTo(minSingleAmount) >= 0 ?
                    availableCash : BigDecimal.ZERO;
            if(buyAmount.compareTo(BigDecimal.ZERO) == 0) {
                //没有足够的资金，不再购买
                log.warn("没有足够的资金，不再购买 {} {}", passedStock.getSymbol(), cal.getCalDate());
                //未处理的pickIds
                List<Long> unprocessedPickIds = allPickIds.stream().filter(id -> !pickIds.contains(id)).collect(Collectors.toList());
                if(unprocessedPickIds != null && !unprocessedPickIds.isEmpty()) {
                    stockPickDiagnosticMapper.updateStatusByIds(unprocessedPickIds.toArray(new Long[0]), 2);
                }
                break;
            }

            //改股票是否已经持仓，如果已经持仓则不买了
            TfPosition queryPosition = new TfPosition();
            queryPosition.setAccountNo(backtestRecord.getAccountNo());
            queryPosition.setSymbol(passedStock.getSymbol());
            List<TfPosition> inPosList = tfPositionMapper.selectTfPositionList(queryPosition);
            if(inPosList != null && !inPosList.isEmpty()) {
                log.warn("股票 {} 已经持仓，不再购买 {}", passedStock.getSymbol(), cal.getCalDate());
                stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 2);
                continue;
            }

            boolean buy = true;
            //是否使用AI复核
            boolean useAiReview = backtestRecord.getUseAi() != null && backtestRecord.getUseAi().equals(1);
            if(useAiReview) {
                buy = aiReview(passedStock, cal.getCalDate());
            }
            if(!buy) {
                log.warn("AI复核未通过，不购买 {} {}", passedStock.getSymbol(), cal.getCalDate());
                stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 2);
                continue;
            }

            //获取T日到T + holdDays + 2日的stock的分时k线
            TfStock stock = new TfStock();
            stock.setSymbol(passedStock.getSymbol());
            stock.setName(passedStock.getStockName());
            int fetchDays = backtestRecord.getCnfTrade().getHoldDays().intValue() + 2;
            List<TfKline> klines = aStockKlineFetcher.getKlineSingle(stock, DateUtils.toLocalDate(cal.getCalDate()), fetchDays, "1m");
            if(klines == null || klines.isEmpty()) {
                log.warn("没有找到 {} 的分时k线 {}， 可能停牌了，不买", passedStock.getSymbol(), cal.getCalDate());
                stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 2);
                continue;
            }
            //遍历klines，找到第一个9:30:00的开盘时间
            LocalDateTime openTime = cal.getCalDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(9, 30, 0);
            //10:00:00为买入截止时间
            LocalDateTime buyDeadline = cal.getCalDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(10, 0, 0);
            BigDecimal openPrice = BigDecimal.ZERO;
            LocalDateTime tradeTime = null;
            boolean found = false;
            for(TfKline kline : klines) {
                LocalDateTime klineTradeDate = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                if(klineTradeDate.toLocalDate().equals(DateUtils.toLocalDate(cal.getCalDate())) && !klineTradeDate.isBefore(openTime) && klineTradeDate.isBefore(buyDeadline)) {
                    openPrice = kline.getOpen();
                    tradeTime = klineTradeDate;
                    found = true;
                    break;
                }
            }
            if(!found) {
                log.warn("没有找到 {} 的9:30开盘价 {}， 可能停牌了，不买", passedStock.getSymbol(), cal.getCalDate());
                stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 2);
                continue;
            }
            //如果开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，则不买入
            BigDecimal threshold = backtestRecord.getCnfTrade().getT1HighOpenRatio();
            BigDecimal previousClosePrice = passedStock.getClosePrice();
            if (openPrice.compareTo(previousClosePrice) > 0) {
                if(openPrice.compareTo(previousClosePrice.multiply(threshold.add(BigDecimal.ONE))) > 0) {
                    log.warn("该股票的开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，不买入, {}", passedStock.getStockName());
                    stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 2);
                    continue;
                }
            }

            //买入
            //买入手数，以手为单位，一手100股
            long buyQuantity = buyAmount.divide(openPrice, 0, RoundingMode.HALF_UP).longValue() / 100;
            long buyVolume = buyQuantity * 100;
            //买入金额
            BigDecimal tradeAmount = openPrice.multiply(BigDecimal.valueOf(buyVolume));
            //交易佣金
            BigDecimal tradeCommission = commissionTaxService.getCommission(tradeAmount);
            //过户费
            BigDecimal transferFee = commissionTaxService.getTransferFee(tradeAmount);
            //买入没有印花税
            //总费用
            BigDecimal totalFee = tradeCommission.add(transferFee);

            //持仓
            TfPosition position = new TfPosition();
            position.setAccountNo(backtestRecord.getAccountNo());
            position.setSymbol(passedStock.getSymbol());
            position.setStockName(passedStock.getStockName());
            position.setTotalVolume(buyVolume);
            //可用股數（T+1：當天買入的為 0，次日解凍變為總股數
            position.setAvailableVolume(0L);
            position.setCostPrice(openPrice);
            position.setCurrentPrice(openPrice);
            position.setTargetPrice(passedStock.getTargetPrice());
            position.setStopLossPrice(passedStock.getStopLossPrice());
            position.setMarketValue(tradeAmount);
            position.setOpenDate(DateUtils.toDate(tradeTime));
            position.setUpdateTime(DateUtils.toDate(tradeTime));
            tfPositionMapper.insertTfPosition(position);

            //创建交易交割单
            log.info("买入股票: {}", passedStock.getStockName());
            TfTradeExecution execution = new TfTradeExecution();
            execution.setAccountNo(backtestRecord.getAccountNo());
            execution.setBacktestId(backtestRecord.getId());
            execution.setPositionId(position.getId());
            execution.setSymbol(passedStock.getSymbol());
            execution.setStockName(passedStock.getStockName());
            execution.setTradeSide(TradeType.BUY.getValue());
            execution.setTradePrice(openPrice);
            execution.setTradeVolume(buyVolume);
            execution.setTradeAmount(tradeAmount);
            execution.setCommission(totalFee);
            execution.setTax(BigDecimal.ZERO);
            execution.setTradeTime(DateUtils.toDate(tradeTime));
            execution.setCreateTime(new Date());
            tfTradeExecutionMapper.insertTfTradeExecution(execution);

            //创建现金流量明细-买入
            TfFundFlow fundFlow = new TfFundFlow();
            fundFlow.setAccountNo(backtestRecord.getAccountNo());
            fundFlow.setBacktestId(backtestRecord.getId());
            fundFlow.setTradeDate(DateUtils.toDate(tradeTime));
            fundFlow.setBizType("BUY");
            fundFlow.setAmount(tradeAmount);
            fundFlow.setBalanceAfter(accountTemp.getAvailableCash());
            fundFlow.setRelatedOrderId(String.valueOf(execution.getId()));
            fundFlow.setCreateTime(DateUtils.toDate(tradeTime));
            tfFundFlowMapper.insertTfFundFlow(fundFlow);

            //创建现金流量明细-支持手续费
            TfFundFlow fundFlowFee = new TfFundFlow();
            fundFlowFee.setAccountNo(backtestRecord.getAccountNo());
            fundFlowFee.setBacktestId(backtestRecord.getId());
            fundFlowFee.setTradeDate(DateUtils.toDate(tradeTime));
            fundFlowFee.setBizType("COMMISSION");
            fundFlowFee.setAmount(totalFee);
            fundFlowFee.setBalanceAfter(accountTemp.getAvailableCash());
            fundFlowFee.setRelatedOrderId(String.valueOf(execution.getId()));
            fundFlowFee.setCreateTime(DateUtils.toDate(tradeTime));
            tfFundFlowMapper.insertTfFundFlow(fundFlowFee);

            //更新账户信息
            //可用金额减去总费用+买入金额
            accountTemp.setAvailableCash(accountTemp.getAvailableCash().subtract(totalFee).subtract(tradeAmount));
            //持仓市值增加tradeAmount
            accountTemp.setMarketValue(accountTemp.getMarketValue().add(tradeAmount));
            //更新账户总金额
            accountTemp.setTotalAssets(accountTemp.getAvailableCash().add(accountTemp.getMarketValue()));
            //累计盈亏金额和累计盈亏比例等到卖出才更新
            accountTemp.setUpdateTime(new Date());
            tfAccountMapper.updateTfAccount(accountTemp);

            //最后更新pick状态为已选中
            stockPickDiagnosticMapper.updateStatusByIds(new Long[]{passedStock.getId()}, 1);
        }
    }

    private boolean aiReview(StockPickDiagnostic passedStock, Date currentDate) {
        //获取风险公告的时间：currentDate的9:15:00之前
        LocalDateTime tradeTime = currentDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(9, 15, 0);
        List<TfRiskAnn> riskAnn = tfRiskAnnService.getRiskAnnFromEastMoneyBackward(
                passedStock.getSymbol(), DateUtils.toDate(tradeTime), 30, 3);
        RiskReport report = tfRiskReportService.aiTradeReportSync(passedStock, riskAnn);
        if(report == null) {
            return true;
        }
        return report.getPassed();
    }

    /**
     * 卖出逻辑
     */
    private void sellLogic(TfBacktestRecord backtestRecord, TaCal cal, SseEmitter sseEmitter) {
        //此方法已经判定了cal为交易日，不是休市日
        log.info("开始执行卖出逻辑，日期：{}", cal.getCalDate());

        //从持仓中获取需要卖出的股票列表
        TfPosition queryPosition = new TfPosition();
        queryPosition.setAccountNo(backtestRecord.getAccountNo());
        //日期在cal之前，不包括cal
        Date endDate = DateUtils.addDays(cal.getCalDate(), -1);
        queryPosition.setEndDate(endDate);
        List<TfPosition> sellablePositions = tfPositionMapper.selectTfPositionList(queryPosition);
        if(sellablePositions == null || sellablePositions.isEmpty()) {
            log.info("没有找到正在持仓且可卖出（T+1制）的股票");
            return;
        }
        //遍历持仓列表，进行卖出
        for(TfPosition position : sellablePositions) {
            log.info("开始执行卖出逻辑，股票：{}", position.getStockName());
            //获取股票的分时k线，时间在cal当天
            TfStock stock = new TfStock();
            stock.setSymbol(position.getSymbol());
            stock.setName(position.getStockName());
            List<TfKline> klines = aStockKlineFetcher.getKlineSingle(stock, DateUtils.toLocalDate(cal.getCalDate()), 1, "1m");
            if(klines == null || klines.isEmpty()) {
                log.warn("没有找到 {} 的分时k线 {}， 可能停牌了，卖不了", position.getSymbol(), cal.getCalDate());
                continue;
            }
            //止盈价
            BigDecimal stopProfitPrice = BigDecimal.ZERO;
            //止损价
            BigDecimal stopLossPrice = BigDecimal.ZERO;

            //获取交易策略, 查看是按固定止盈止损比率还是按动态计算止盈止损价卖出
            TfConfigStrategyTrade cnfTrade = backtestRecord.getCnfTrade();
            //止盈方法：1固定幅度，2动态幅度
            if(cnfTrade.getStopProfitMethod().equals(1)) {
                //固定比率止盈
                stopProfitPrice = position.getCostPrice().multiply(cnfTrade.getStopProfitRatio().add(BigDecimal.ONE));
            } else {
                //策略动态止盈
                stopProfitPrice = position.getTargetPrice();
            }

            //止损方法：1固定止损，2动态止损
            if(cnfTrade.getStopLossMethod().equals(1)) {
                //固定比率止损
                stopLossPrice = position.getCostPrice().multiply(BigDecimal.ONE.subtract(cnfTrade.getStopLossRatio()));
            } else {
                //策略动态止损
                stopLossPrice = position.getStopLossPrice();
            }

            LocalDateTime openTime = cal.getCalDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().atTime(9, 30, 0);
            //10:00:00为买入截止时间
            LocalDateTime sellDeadline = cal.getCalDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().atTime(14, 55, 0);
            //逐条K线
            for(TfKline kline : klines) {
                //k线的open价大于目标止盈价就卖
                //9:30之前是卖不了的
                LocalDateTime klineTime = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                if(!klineTime.isAfter(openTime)) {
                    log.warn("k线时间在9:30之前，卖不了 {}", klineTime);
                    continue;
                }
                //也不要等到收市之前最后5分钟卖
                if(klineTime.isAfter(sellDeadline)) {
                    log.warn("k线时间在收市之前最后5分钟，卖不了 {}", klineTime);
                    break;
                }
                BigDecimal klineOpenPrice = kline.getOpen();
                //如果k线的open价大于等于目标止盈价就卖
                if(klineOpenPrice.compareTo(stopProfitPrice) >= 0) {
                    doSell(backtestRecord, cal, position, kline, SellReason.PROFIT, sseEmitter);
                    break;
                }

                //如果k线的open价小于等于目标止损价就卖
                if(klineOpenPrice.compareTo(stopLossPrice) <= 0) {
                    doSell(backtestRecord, cal, position, kline, SellReason.LOSS, sseEmitter);
                    break;
                }

                //如果已经持仓了holdDays，但都没有止盈或止损，则在最后一天的14:30分后按当前价平仓
                LocalDateTime lastChanceTime = cal.getCalDate().toInstant().plus(cnfTrade.getHoldDays(), ChronoUnit.DAYS)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().atTime(14, 30, 0);
                if(klineTime.isAfter(lastChanceTime)) {
                    doSell(backtestRecord, cal, position, kline, SellReason.TIMEOUT, sseEmitter);
                    break;
                }
            }
        }
    }

    private void doSell(TfBacktestRecord backtestRecord, TaCal cal, TfPosition position, TfKline kline, SellReason reason, SseEmitter sseEmitter) {
        log.info("卖出股票: {}", position.getStockName());

        //通过持仓id获取买入交割单信息
        TfTradeExecution queryBuyExecution = new TfTradeExecution();
        queryBuyExecution.setBacktestId(backtestRecord.getId());
        queryBuyExecution.setAccountNo(backtestRecord.getAccountNo());
        queryBuyExecution.setPositionId(position.getId());
        queryBuyExecution.setTradeSide(TradeType.BUY.getValue());
        List<TfTradeExecution> buyExecutions = tfTradeExecutionMapper.selectTfTradeExecutionList(queryBuyExecution);
        TfTradeExecution buyExecution = null;
        if(buyExecutions != null && !buyExecutions.isEmpty()) {
            buyExecution = buyExecutions.get(0);
        }
        if(buyExecution == null) {
            log.warn("没有找到对应的买入交割单信息 {}", position.getStockName());
            return;
        }

        LocalDateTime klineTime = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();

        //全部卖出
        long sellVolume = position.getAvailableVolume().longValue();
        //卖出价格
        BigDecimal sellPrice = kline.getOpen();
        //卖出金额
        BigDecimal sellAmount = sellPrice.multiply(BigDecimal.valueOf(sellVolume));

        //交易佣金/手续费
        BigDecimal sellCommission = commissionTaxService.getCommission(sellAmount);
        //过户费
        BigDecimal transferFee = commissionTaxService.getTransferFee(sellAmount);
        //总费用
        BigDecimal totalSellFee = sellCommission.add(transferFee);
        //印花税
        BigDecimal stampTax = commissionTaxService.getTax(sellAmount);

        //本次卖出回笼资金
        BigDecimal returnAmount = sellAmount.subtract(totalSellFee).subtract(stampTax);

        //买入金额=买入交割单的成交金额
        BigDecimal buyAmount = buyExecution.getTradeAmount();
        //总费用
        BigDecimal bothFee = totalSellFee.add(stampTax).add(buyExecution.getCommission());
        //盈利=卖出金额-买入金额-总费用,有可能为负
        BigDecimal profit = sellAmount.subtract(buyAmount).subtract(bothFee);

        //创建卖出执行记录
        TfTradeExecution sellExecution = new TfTradeExecution();
        sellExecution.setAccountNo(buyExecution.getAccountNo());
        sellExecution.setBacktestId(backtestRecord.getId());
        //方便查询，买卖同一个持仓id
        sellExecution.setPositionId(buyExecution.getPositionId());
        sellExecution.setSymbol(buyExecution.getSymbol());
        sellExecution.setStockName(buyExecution.getStockName());
        sellExecution.setTradeSide(TradeType.SELL.getValue());
        sellExecution.setTradeReason(reason.getCode());
        //以k线的开盘价为卖出价格
        sellExecution.setTradePrice(sellPrice);
        sellExecution.setTradeVolume(sellVolume);
        sellExecution.setTradeAmount(sellAmount);
        sellExecution.setCommission(totalSellFee);
        sellExecution.setTax(stampTax);
        sellExecution.setRealizedPnl(profit);
        sellExecution.setTradeTime(DateUtils.toDate(klineTime));
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
        account.setUpdateTime(DateUtils.toDate(klineTime));

        tfAccountMapper.updateTfAccount(account);

        //创建现金流量明细-卖出
        TfFundFlow fundFlow = new TfFundFlow();
        fundFlow.setAccountNo(backtestRecord.getAccountNo());
        fundFlow.setBacktestId(backtestRecord.getId());
        fundFlow.setTradeDate(DateUtils.toDate(klineTime));
        fundFlow.setBizType("SELL");
        fundFlow.setAmount(sellAmount);
        fundFlow.setBalanceAfter(account.getAvailableCash());
        fundFlow.setRelatedOrderId(String.valueOf(sellExecution.getId()));
        fundFlow.setCreateTime(DateUtils.toDate(klineTime));
        tfFundFlowMapper.insertTfFundFlow(fundFlow);

        //创建现金流量明细-支持手续费
        TfFundFlow fundFlowFee = new TfFundFlow();
        fundFlowFee.setAccountNo(backtestRecord.getAccountNo());
        fundFlowFee.setBacktestId(backtestRecord.getId());
        fundFlowFee.setTradeDate(DateUtils.toDate(klineTime));
        fundFlowFee.setBizType("COMMISSION");
        fundFlowFee.setAmount(totalSellFee);
        fundFlowFee.setBalanceAfter(account.getAvailableCash());
        fundFlowFee.setRelatedOrderId(String.valueOf(sellExecution.getId()));
        fundFlowFee.setCreateTime(DateUtils.toDate(klineTime));
        tfFundFlowMapper.insertTfFundFlow(fundFlowFee);

        //创建现金流量明细-印花税
        TfFundFlow fundFlowTax = new TfFundFlow();
        fundFlowTax.setAccountNo(backtestRecord.getAccountNo());
        fundFlowTax.setBacktestId(backtestRecord.getId());
        fundFlowTax.setTradeDate(DateUtils.toDate(klineTime));
        fundFlowTax.setBizType("TAX");
        fundFlowTax.setAmount(stampTax);
        fundFlowTax.setBalanceAfter(account.getAvailableCash());
        fundFlowTax.setRelatedOrderId(String.valueOf(sellExecution.getId()));
        fundFlowTax.setCreateTime(DateUtils.toDate(klineTime));
        tfFundFlowMapper.insertTfFundFlow(fundFlowTax);
    }

    /**
     * 选股逻辑
     */
    private void pickLogic(TfBacktestRecord backtestRecord, TaCal cal, SseEmitter sseEmitter) {
        //1 如果在当天交易日算起到（策略持仓天数+1天减仓操作）的时间内，遇上法定节假日，则不选了
    }
}
