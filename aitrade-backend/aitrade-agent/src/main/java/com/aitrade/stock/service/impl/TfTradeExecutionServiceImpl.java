package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfTradeExecutionMapper;
import com.aitrade.stock.domain.TfTradeExecution;
import com.aitrade.stock.service.ITfTradeExecutionService;

/**
 * 回測歷史成交交割單Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Service
public class TfTradeExecutionServiceImpl implements ITfTradeExecutionService 
{
    @Autowired
    private TfTradeExecutionMapper tfTradeExecutionMapper;

    /**
     * 查询回測歷史成交交割單
     * 
     * @param id 回測歷史成交交割單主键
     * @return 回測歷史成交交割單
     */
    @Override
    public TfTradeExecution selectTfTradeExecutionById(Long id)
    {
        return tfTradeExecutionMapper.selectTfTradeExecutionById(id);
    }

    /**
     * 查询回測歷史成交交割單列表
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 回測歷史成交交割單
     */
    @Override
    public List<TfTradeExecution> selectTfTradeExecutionList(TfTradeExecution tfTradeExecution)
    {
        return tfTradeExecutionMapper.selectTfTradeExecutionList(tfTradeExecution);
    }

    /**
     * 新增回測歷史成交交割單
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 结果
     */
    @Override
    public int insertTfTradeExecution(TfTradeExecution tfTradeExecution)
    {
        tfTradeExecution.setCreateTime(DateUtils.getNowDate());
        return tfTradeExecutionMapper.insertTfTradeExecution(tfTradeExecution);
    }

    /**
     * 修改回測歷史成交交割單
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 结果
     */
    @Override
    public int updateTfTradeExecution(TfTradeExecution tfTradeExecution)
    {
        return tfTradeExecutionMapper.updateTfTradeExecution(tfTradeExecution);
    }

    /**
     * 批量删除回測歷史成交交割單
     * 
     * @param ids 需要删除的回測歷史成交交割單主键
     * @return 结果
     */
    @Override
    public int deleteTfTradeExecutionByIds(Long[] ids)
    {
        return tfTradeExecutionMapper.deleteTfTradeExecutionByIds(ids);
    }

    /**
     * 删除回測歷史成交交割單信息
     * 
     * @param id 回測歷史成交交割單主键
     * @return 结果
     */
    @Override
    public int deleteTfTradeExecutionById(Long id)
    {
        return tfTradeExecutionMapper.deleteTfTradeExecutionById(id);
    }
}
