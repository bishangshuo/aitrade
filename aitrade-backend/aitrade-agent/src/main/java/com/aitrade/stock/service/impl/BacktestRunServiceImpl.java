package com.aitrade.stock.service.impl;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.risk.domain.RiskReport;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;
import com.aitrade.risk.service.ITfRiskReportService;
import com.aitrade.stock.domain.*;
import com.aitrade.stock.domain.vo.HolidayGap;
import com.aitrade.stock.mapper.StockPickDiagnosticMapper;
import com.aitrade.stock.mapper.TfAccountMapper;
import com.aitrade.stock.mapper.TfPositionDailyMapper;
import com.aitrade.stock.mapper.TfPositionMapper;
import com.aitrade.stock.service.IBacktestRunService;
import com.aitrade.stock.service.ITaCalService;
import com.aitrade.stock.service.ITfAccountDailyService;
import com.aitrade.stock.service.ITfPositionService;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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

        boolean firstDay = true;
        boolean lastDay = false;
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

                /**
                 * 3. 执行选股
                 */
                if(holidayGap == null) {
                    pickLogic(backtestRecord, cal, emitter);
                }
            }

            /**
             * 4. 更新账户日历 和 持仓日历
             */
            updateAccountAndPositionDaily(backtestRecord, cal, firstDay);

            firstDay = false;
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
    private void updateAccountAndPositionDaily(TfBacktestRecord backtestRecord, TaCal cal, boolean firstDay) {

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
        for(StockPickDiagnostic passedStock : passedStocks) {
            if(bought > diversification.intValue()) {
                log.warn("已购买10只股票，不再购买 {}", cal.getCalDate());
                return;
            }

            //获取账户信息，用于判断可用资金是否足够, 触发等级最高
            TfAccount accountTemp = tfAccountMapper.selectTfAccountByNo(backtestRecord.getAccountNo());
            BigDecimal availableCash = accountTemp.getAvailableCash();
            BigDecimal buyAmount = availableCash.compareTo(maxSingleAmount) > 0 ?
                    maxSingleAmount : availableCash.compareTo(minSingleAmount) >= 0 ?
                    availableCash : BigDecimal.ZERO;
            if(buyAmount.compareTo(BigDecimal.ZERO) == 0) {
                //没有足够的资金，不再购买
                log.warn("没有足够的资金，不再购买 {} {}", passedStock.getSymbol(), cal.getCalDate());
                break;
            }

            //改股票是否已经持仓，如果已经持仓则不买了
            TfPosition queryPosition = new TfPosition();
            queryPosition.setAccountNo(backtestRecord.getAccountNo());
            queryPosition.setSymbol(passedStock.getSymbol());
            List<TfPosition> inPosList = tfPositionMapper.selectTfPositionList(queryPosition);
            if(inPosList != null && !inPosList.isEmpty()) {
                log.warn("股票 {} 已经持仓，不再购买 {}", passedStock.getSymbol(), cal.getCalDate());
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
                continue;
            }
            //遍历klines，找到第一个9:30:00的开盘时间
            LocalDate openTime = LocalDate.of(9, 30, 0);
            BigDecimal openPrice = BigDecimal.ZERO;
            LocalDate tradeTime = null;
            boolean found = false;
            for(TfKline kline : klines) {
                LocalDate klineTradeDate = Instant.ofEpochMilli(kline.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
                if(klineTradeDate.equals(DateUtils.toLocalDate(cal.getCalDate())) && !klineTradeDate.isBefore(openTime)) {
                    openPrice = kline.getOpen();
                    tradeTime = klineTradeDate;
                    found = true;
                    break;
                }
            }
            if(!found) {
                log.warn("没有找到 {} 的9:30开盘价 {}， 可能停牌了，不买", passedStock.getSymbol(), cal.getCalDate());
                continue;
            }
            //如果开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，则不买入
            BigDecimal threshold = backtestRecord.getCnfTrade().getT1HighOpenRatio();
            BigDecimal previousClosePrice = passedStock.getClosePrice();
            if (openPrice.compareTo(previousClosePrice) > 0) {
                if(openPrice.compareTo(previousClosePrice.multiply(threshold.add(BigDecimal.ONE))) > 0) {
                    log.warn("该股票的开盘价比前一个交易日的收盘价高超过策略参数设定的阈值，不买入, {}", passedStock.getStockName());
                    continue;
                }
            }

            //买入

        }
    }

    private boolean aiReview(StockPickDiagnostic passedStock, Date currentDate) {
        //获取风险公告的时间：currentDate的9:15:00之前
        Date tradeDate = DateUtils.parseDate(DateFormatUtils.format(currentDate, "yyyy-MM-dd 09:15:00"));
        List<TfRiskAnn> riskAnn = tfRiskAnnService.getRiskAnnFromEastMoneyBackward(
                passedStock.getSymbol(), tradeDate, 30, 3);
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
        //1 如果在当天交易日算起到（策略持仓天数+1天减仓操作）的时间内，遇上法定节假日，则不卖了
    }

    /**
     * 选股逻辑
     */
    private void pickLogic(TfBacktestRecord backtestRecord, TaCal cal, SseEmitter sseEmitter) {
        //1 如果在当天交易日算起到（策略持仓天数+1天减仓操作）的时间内，遇上法定节假日，则不选了
    }
}
