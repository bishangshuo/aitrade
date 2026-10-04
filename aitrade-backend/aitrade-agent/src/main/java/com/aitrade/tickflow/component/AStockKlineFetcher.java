package com.aitrade.tickflow.component;

import com.aitrade.common.core.domain.HttpResponse;
import com.aitrade.common.utils.http.HttpUtils;
import com.aitrade.tickflow.domain.TfExchange;
import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
import com.aitrade.tickflow.enums.TfKlinePeriod;
import com.aitrade.tickflow.repository.AStockKlineRepository;
import com.aitrade.tickflow.repository.TfStockRepository;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AStockKlineFetcher {

    @Value("${tickflow.service.url}")
    private String tfService;

    @Value("${tickflow.service.api-key}")
    private String apiKey;

    @Value("${tickflow.service.delay}")
    private int delay;

    @Value("${tickflow.filter}")
    private String filter;

    @Autowired
    private AStockKlineRepository aStockKlineRepository;

    @Autowired
    private TfStockRepository tfStockRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long FIVE_YEARS_MILLIS = 5L * 365 * 24 * 60 * 60 * 1000;


//    @Scheduled(fixedDelay = 8000)
    public void fetch() {
        log.info("开始新一轮K线数据获取");
        // 获取交易所列表
        List<TfExchange> exchanges = getExchanges();
        if(exchanges == null || exchanges.isEmpty()) {
            log.warn("交易所列表获取为空， 结束过程");
            return;
        }
        log.info("获取交易所列表：{}", exchanges.stream().map(TfExchange::getExchange).collect(Collectors.toList()));
        List<String> filterList = Arrays.asList(filter.split(","));
        // 遍历交易所列表
        for (TfExchange exchange : exchanges) {
            //过滤掉美股、港股
            if(filterList.contains(exchange.getExchange())) {
                log.info("过滤掉 {} 交易所", exchange.getExchange());
                continue;
            }
            getAllKlineOfExchange(exchange.getExchange());
        }
    }

    private String getWithDelay(String apiName, Map<String, String> params) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", apiKey);
        headers.put("content-type", "application/json");
        String url = tfService + apiName;
        HttpResponse response = HttpUtils.doGet(url, params, headers);
        if (response.isSuccess()){
            return response.getBody();
        }
        return null;
    }

    /**
     * 获取交易所列表
     * @return
     */

    private List<TfExchange> getExchanges() {
        String result = getWithDelay("/v1/exchanges", null);
        JSONObject jsonObject = JSONObject.parseObject(result);
        if(jsonObject == null) {
            return null;
        }
        JSONArray exchangeArr = jsonObject.getJSONArray("data");
        if(exchangeArr == null || exchangeArr.isEmpty()) {
            return null;
        }
        //exchangeArr转换为List<TfExchange>
        return exchangeArr.toJavaList(TfExchange.class);
    }

    /**
     * 获取标的列表
     * @param exchange
     * @return
     */
    private List<TfStock> getStockList(String exchange) {
        String result = getWithDelay("/v1/exchanges/" + exchange + "/instruments", null);
        JSONObject jsonObject = JSONObject.parseObject(result);
        if(jsonObject == null) {
            return null;
        }
        JSONArray symbolArr = jsonObject.getJSONArray("data");
        if(symbolArr == null || symbolArr.isEmpty()) {
            return null;
        }
        return symbolArr.toJavaList(TfStock.class);
    }

    /**
     * 获取K线数据，批量
     * @param stockList
     * @return
     */
    private List<TfKline> getKlineBatch(List<TfStock> stockList) {
        // 将symbolList转换为逗号分隔的字符串
        List<String> symbolList = stockList.stream().map(TfStock::getSymbol).collect(Collectors.toList());
        String symbolString = String.join(",", symbolList);
        // 五年前日期，时间戳，毫秒
        long startTime = System.currentTimeMillis() - FIVE_YEARS_MILLIS;
        String apiName = "/v1/klines/batch?symbols=" + symbolString + "&period=1d&start_time=" + startTime + "&count=10000";
        String result = getWithDelay(apiName, null);
        if(result == null || result.isEmpty()) {
            log.warn("获取({})k线数据发生错误， 跳过", symbolString);
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        if(jsonObject == null) {
            log.warn("获取({})K线数据json格式错误， 跳过", symbolString);
            return null;
        }
        JSONObject jsonData = jsonObject.getJSONObject("data");
        if(jsonData == null) {
            log.warn("获取({})K线数据data为空， 跳过", symbolString);
            return null;
        }

        List<TfKline> tfKlineList = new ArrayList<>();
        for(TfStock stock : stockList) {
            JSONObject symbolData = jsonData.getJSONObject(stock.getSymbol());
            if(symbolData == null) {
                log.warn("获取({})k线数据发生错误,jsonData.getJSONObject为空， 跳过", stock.getSymbol());
                continue;
            }
            JSONArray timestampArr = symbolData.getJSONArray("timestamp");
            JSONArray openArr = symbolData.getJSONArray("open");
            JSONArray highArr = symbolData.getJSONArray("high");
            JSONArray lowArr = symbolData.getJSONArray("low");
            JSONArray closeArr = symbolData.getJSONArray("close");
            JSONArray volumeArr = symbolData.getJSONArray("volume");
            JSONArray amountArr = symbolData.getJSONArray("amount");

            for(int i = 0; i < timestampArr.size(); i++) {
                TfKline tfKline = new TfKline();
                tfKline.setSymbol(stock.getSymbol());
                tfKline.setStockName(stock.getName());
                tfKline.setTimestamp(timestampArr.getLong(i));
                tfKline.setOpen(BigDecimal.valueOf(openArr.getDouble(i)));
                tfKline.setHigh(BigDecimal.valueOf(highArr.getDouble(i)));
                tfKline.setLow(BigDecimal.valueOf(lowArr.getDouble(i)));
                tfKline.setClose(BigDecimal.valueOf(closeArr.getDouble(i)));
                tfKline.setVolume(BigDecimal.valueOf(volumeArr.getDouble(i)));
                tfKline.setAmount(BigDecimal.valueOf(amountArr.getDouble(i)));
                tfKlineList.add(tfKline);
            }
        }
        return tfKlineList;
    }

    /**
     * 获取交易所所有标的的K线数据
     * @param exchange
     * @return
     */
    private void getAllKlineOfExchange(String exchange) {
        log.info("开始获取交易所内所有股票的日k数据:{}", exchange);
        List<TfStock> stockList = getStockList(exchange);
        if(stockList == null) {
            log.warn("交易所 {} 标的列表为空， 跳过", exchange);
            return;
        }
        log.info("交易所 {} 标的列表（size={}): {}", exchange, stockList.size(), stockList.stream().map(TfStock::getSymbol).collect(Collectors.toList()));
        //保存/更新股票信息
        tfStockRepository.batchSave(stockList);
        int batchSize = 5;
        for(int i = 0; i < stockList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, stockList.size());
            List<TfStock> subList = stockList.subList(i, end);
            if(subList.isEmpty()) {
                break;
            }
            log.info("开始获取标的列表（size={}）的日k数据: {}", subList.size(), subList.stream().map(TfStock::getSymbol).collect(Collectors.toList()));
            List<TfKline> tfKlineList = getKlineBatch(subList);
            log.info("获取标的列表（size={}）的日k数据完成: {}", subList.size(), tfKlineList);
            //保存到数据库
            aStockKlineRepository.batchSave(TfKlinePeriod.DAY_1, tfKlineList);
            log.info("插入数据库完成");
        }
    }
}
