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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
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

        //遍历
        int bought = 0;
        for(StockPickDiagnostic passedStock : passedStocks) {
            if(bought > 10) {
                log.warn("已购买10只股票，不再购买 {}", cal.getCalDate());
                return;
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
            //
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
