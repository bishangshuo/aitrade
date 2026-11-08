package com.aitrade.stock.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.stock.domain.TfTradeExecution;
import com.aitrade.stock.mapper.TfTradeExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfPositionMapper;
import com.aitrade.stock.domain.TfPosition;
import com.aitrade.stock.service.ITfPositionService;

/**
 * 回測當前持倉Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Service
@Slf4j
public class TfPositionServiceImpl implements ITfPositionService 
{
    @Autowired
    private TfPositionMapper tfPositionMapper;

    @Autowired
    private TfTradeExecutionMapper tfTradeExecutionMapper;

    /**
     * 查询回測當前持倉
     * 
     * @param id 回測當前持倉主键
     * @return 回測當前持倉
     */
    @Override
    public TfPosition selectTfPositionById(Long id)
    {
        return tfPositionMapper.selectTfPositionById(id);
    }

    /**
     * 查询回測當前持倉列表
     * 
     * @param tfPosition 回測當前持倉
     * @return 回測當前持倉
     */
    @Override
    public List<TfPosition> selectTfPositionList(TfPosition tfPosition)
    {
        return tfPositionMapper.selectTfPositionList(tfPosition);
    }

    /**
     * 新增回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    @Override
    public int insertTfPosition(TfPosition tfPosition)
    {
        tfPosition.setCreateTime(DateUtils.getNowDate());
        return tfPositionMapper.insertTfPosition(tfPosition);
    }

    /**
     * 修改回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    @Override
    public int updateTfPosition(TfPosition tfPosition)
    {
        tfPosition.setUpdateTime(DateUtils.getNowDate());
        return tfPositionMapper.updateTfPosition(tfPosition);
    }

    /**
     * 批量删除回測當前持倉
     * 
     * @param ids 需要删除的回測當前持倉主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionByIds(Long[] ids)
    {
        return tfPositionMapper.deleteTfPositionByIds(ids);
    }

    /**
     * 删除回測當前持倉信息
     * 
     * @param id 回測當前持倉主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionById(Long id)
    {
        return tfPositionMapper.deleteTfPositionById(id);
    }

    /**
     * 检查持仓: 检查股票是否在持仓中
     * 由于是回测，在买入的同时已经卖出了，持仓记录已经不存在，所以使用TfTradeExecution来查询
     * @param backtestId
     * @param symbol
     * @param tradeDate 交易日
     * @return
     */
    @Override
    public boolean checkInPosition(Long backtestId, String symbol, LocalDate tradeDate) {
        TfTradeExecution execution = new TfTradeExecution();
        execution.setBacktestId(backtestId);
        execution.setSymbol(symbol);
        List<TfTradeExecution> executionList = tfTradeExecutionMapper.selectTfTradeExecutionList(execution);
        if(executionList == null || executionList.isEmpty()) {
            return false;
        }

        //对executionList进行分组，以order_id为一组，每组一对
        Map<String, List<TfTradeExecution>> execGroups = executionList.stream()
            .collect(Collectors.groupingBy(TfTradeExecution::getOrderId));

        //只要tradeDate坐落在某一个组的买入时间和卖出时间之间，则认为在持仓中
        for(List<TfTradeExecution> execGroup : execGroups.values()) {
            //已经进行了买卖
            if(execGroup != null && execGroup.size() == 2) {
                TfTradeExecution buyExec = execGroup.get(0);
                LocalDate buyTime = DateUtils.toLocalDate(buyExec.getTradeTime());
                TfTradeExecution sellExec = execGroup.get(1);
                LocalDate sellTime = DateUtils.toLocalDate(sellExec.getTradeTime());
                if(buyTime.isBefore(tradeDate) && sellTime.isAfter(tradeDate)) {
                    return true;
                }
            }
            //只有买，没有卖，在整个回测周期里，可能被套住了几年，这是极端的情况
            else {
                log.warn("只有买，没有卖，可能被套住了几年，这是极端的情况");
            }
        }
        return false;
    }
}
