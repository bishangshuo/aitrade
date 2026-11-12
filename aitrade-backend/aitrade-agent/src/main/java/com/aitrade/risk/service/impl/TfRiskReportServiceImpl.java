package com.aitrade.risk.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.common.utils.JsonStreamDetector;
import com.aitrade.rag.component.AgentContainer;
import com.aitrade.rag.enums.AgentEnum;
import com.aitrade.rag.enums.AgentParamType;
import com.aitrade.rag.service.IRagApiService;
import com.aitrade.rag.utils.AgentParamBuilder;
import com.aitrade.risk.domain.RiskReport;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.strategy.enums.StrategyType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.risk.mapper.TfRiskReportMapper;
import com.aitrade.risk.domain.TfRiskReport;
import com.aitrade.risk.service.ITfRiskReportService;

/**
 * AI研报Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Service
@Slf4j
public class TfRiskReportServiceImpl implements ITfRiskReportService 
{
    @Autowired
    private TfRiskReportMapper tfRiskReportMapper;

    @Autowired
    private IRagApiService ragApiService;
    @Autowired
    private AgentContainer agentContainer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询AI研报
     * 
     * @param id AI研报主键
     * @return AI研报
     */
    @Override
    public TfRiskReport selectTfRiskReportById(Long id)
    {
        return tfRiskReportMapper.selectTfRiskReportById(id);
    }

    /**
     * 查询AI研报列表
     * 
     * @param tfRiskReport AI研报
     * @return AI研报
     */
    @Override
    public List<TfRiskReport> selectTfRiskReportList(TfRiskReport tfRiskReport)
    {
        return tfRiskReportMapper.selectTfRiskReportList(tfRiskReport);
    }

    /**
     * 新增AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    @Override
    public int insertTfRiskReport(TfRiskReport tfRiskReport)
    {
        tfRiskReport.setCreateTime(DateUtils.getNowDate());
        return tfRiskReportMapper.insertTfRiskReport(tfRiskReport);
    }

    /**
     * 修改AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    @Override
    public int updateTfRiskReport(TfRiskReport tfRiskReport)
    {
        return tfRiskReportMapper.updateTfRiskReport(tfRiskReport);
    }

    /**
     * 批量删除AI研报
     * 
     * @param ids 需要删除的AI研报主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportByIds(Long[] ids)
    {
        return tfRiskReportMapper.deleteTfRiskReportByIds(ids);
    }

    /**
     * 删除AI研报信息
     * 
     * @param id AI研报主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportById(Long id)
    {
        return tfRiskReportMapper.deleteTfRiskReportById(id);
    }

    @Override
    public void aiTradeReport(
            AtomicBoolean cancelled,
        StockPickDiagnostic stockPickDiagnostic,
        List<TfRiskAnn> tfRiskAnnList,
        Function<String, Boolean> streamHandler,
        Function<RiskReport, Boolean> resultHandler,
        Runnable onError
    ) {
        try {
            String agentId = agentContainer.getAgentId(AgentEnum.AGENT_FILTER_RISK_ANNOUNCE.getTitle());
            Map<String, Object> inputs = new HashMap<>();

            String query = "";

            DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String tradeDate = stockPickDiagnostic.getTradeDate().format(FORMATTER);
            String strategyType = StrategyType.getByCode(stockPickDiagnostic.getStrategyCode()).getDescription();
            String riskAnnouce = objectMapper.writeValueAsString(tfRiskAnnList);
            String stockPickDiagnosticContent = stockPickDiagnostic.buildSummaryText();

            AgentParamBuilder.makeAgentInputParam("stockCode", AgentParamType.LINE.getCode(), stockPickDiagnostic.getSymbol(), inputs);
            AgentParamBuilder.makeAgentInputParam("stockName", AgentParamType.LINE.getCode(), stockPickDiagnostic.getStockName(), inputs);
            AgentParamBuilder.makeAgentInputParam("tradeDate", AgentParamType.LINE.getCode(), tradeDate, inputs);
            AgentParamBuilder.makeAgentInputParam("strategyType", AgentParamType.LINE.getCode(), strategyType, inputs);
            AgentParamBuilder.makeAgentInputParam("riskAnnouce", AgentParamType.JSON.getCode(), riskAnnouce, inputs);
            AgentParamBuilder.makeAgentInputParam("stockPickDiagnostic", AgentParamType.JSON.getCode(), stockPickDiagnosticContent, inputs);

            StringBuffer buffer = new StringBuffer();
            ragApiService.agentStream(
                    agentId,
                    query,
                    inputs,
                    content -> {
                        String answer = JsonStreamDetector.getMessageFromAgentData(content);
                        if (answer == null || answer.isEmpty()) {
                            return true;
                        }
                        buffer.append(answer);

                        if(streamHandler != null) {
                            streamHandler.apply(answer);
                        }
                        return true;
                    },
                    () -> {
                        if(onError != null) {
                            onError.run();
                        }
                    },
                    () -> {
                        String result = buffer.toString();
                        RiskReport report = null;
                        try {
                             report = objectMapper.readValue(result, RiskReport.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        if(resultHandler != null) {
                            resultHandler.apply(report);
                        }
                    },
                    cancelled
            );
        }
        catch (Exception e) {
            log.error("ai复核失败: {}", e);
            if(onError != null) {
                onError.run();
            }
        }
    }

    @Override
    public RiskReport aiTradeReportSync(
        StockPickDiagnostic stockPickDiagnostic,
        List<TfRiskAnn> tfRiskAnnList
    ) {
        try {
            String agentId = agentContainer.getAgentId(AgentEnum.AGENT_FILTER_RISK_ANNOUNCE.getTitle());
            Map<String, Object> inputs = new HashMap<>();

            String query = "";

            DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String tradeDate = stockPickDiagnostic.getTradeDate().format(FORMATTER);
            String strategyType = StrategyType.getByCode(stockPickDiagnostic.getStrategyCode()).getDescription();
            String riskAnnouce = objectMapper.writeValueAsString(tfRiskAnnList);
            String stockPickDiagnosticContent = stockPickDiagnostic.buildSummaryText();

            AgentParamBuilder.makeAgentInputParam("stockCode", AgentParamType.LINE.getCode(), stockPickDiagnostic.getSymbol(), inputs);
            AgentParamBuilder.makeAgentInputParam("stockName", AgentParamType.LINE.getCode(), stockPickDiagnostic.getStockName(), inputs);
            AgentParamBuilder.makeAgentInputParam("tradeDate", AgentParamType.LINE.getCode(), tradeDate, inputs);
            AgentParamBuilder.makeAgentInputParam("strategyType", AgentParamType.LINE.getCode(), strategyType, inputs);
            AgentParamBuilder.makeAgentInputParam("riskAnnouce", AgentParamType.JSON.getCode(), riskAnnouce, inputs);
            AgentParamBuilder.makeAgentInputParam("stockPickDiagnostic", AgentParamType.JSON.getCode(), stockPickDiagnosticContent, inputs);
            String response = ragApiService.agent(agentId, query, inputs);
            return objectMapper.readValue(response, RiskReport.class);
        }
        catch (Exception e) {
            return null;
        }
    }

}
