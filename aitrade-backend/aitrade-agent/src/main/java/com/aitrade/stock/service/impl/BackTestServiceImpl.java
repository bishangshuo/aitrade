package com.aitrade.stock.service.impl;

import com.aitrade.common.utils.DateUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Override
    public SseEmitter backTest(LocalDate fromDate, LocalDate toDate) {
        SseEmitter emitter = new SseEmitter();
        //如果toDate比fromDate还早，就抛出异常
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("截止日比开始日还早，错误");
        }

        doRunTest(fromDate, toDate, emitter);

        return emitter;
    }

    private void doRunTest(LocalDate currentDate, LocalDate toDate, SseEmitter emitter) {
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
            doRunTest(nextTradeLocalDate, toDate, emitter);
            return;
        }

        AtomicBoolean isRunning = new AtomicBoolean(false);
        stockTaService.startTA(currentDate, isRunning, new DayTaResultListener() {
            @Override
            public void onComplete() {
                //回测买入、持仓、卖出
                LocalDate nextDay = currentDate.plusDays(1);
                takeAction(currentDate, emitter, new ActionMessageListener() {

                    @Override
                    public void onComplete() {
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(nextDate, toDate, emitter);
                    }

                    @Override
                    public void onFail(String message) {
                        log.warn("当日没有选出符合条件的股票 {}", currentDate);
                        LocalDate nextDate = currentDate.plusDays(1);
                        doRunTest(nextDate, toDate, emitter);
                    }

                    @Override
                    public void onBuy() {

                    }

                    @Override
                    public void onSell() {

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
    private void takeAction(LocalDate currentDate, SseEmitter emitter, ActionMessageListener actionMessageListener) {
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
        //选排前面的10个个股进行买入
        int bought = 0;
        for(StockPickDiagnostic stockPickDiagnostic : passedStocks) {
            //已达到10个买入上线
            if(bought >= 10) {
                break;
            }
            //过滤黑天鹅时间：下载公司公告，使用ai进行深度复核
            //获取T+1日到T+5日的stock的分时k线
            TfStock stock = new TfStock();
            stock.setSymbol(stockPickDiagnostic.getSymbol());
            stock.setName(stockPickDiagnostic.getStockName());
            List<TfKline> klines = aStockKlineFetcher.getKlineSingle(stock, currentDate, 5, "1m");
            //买入
            buyStock(currentDate, emitter, stockPickDiagnostic, klines);
            //卖出

            bought++;
        }
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

    //判断一个股票从第二个交易
}
