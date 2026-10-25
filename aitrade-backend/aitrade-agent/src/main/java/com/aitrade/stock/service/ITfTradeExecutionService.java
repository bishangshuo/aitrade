package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TfTradeExecution;

/**
 * 回測歷史成交交割單Service接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface ITfTradeExecutionService 
{
    /**
     * 查询回測歷史成交交割單
     * 
     * @param id 回測歷史成交交割單主键
     * @return 回測歷史成交交割單
     */
    public TfTradeExecution selectTfTradeExecutionById(Long id);

    /**
     * 查询回測歷史成交交割單列表
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 回測歷史成交交割單集合
     */
    public List<TfTradeExecution> selectTfTradeExecutionList(TfTradeExecution tfTradeExecution);

    /**
     * 新增回測歷史成交交割單
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 结果
     */
    public int insertTfTradeExecution(TfTradeExecution tfTradeExecution);

    /**
     * 修改回測歷史成交交割單
     * 
     * @param tfTradeExecution 回測歷史成交交割單
     * @return 结果
     */
    public int updateTfTradeExecution(TfTradeExecution tfTradeExecution);

    /**
     * 批量删除回測歷史成交交割單
     * 
     * @param ids 需要删除的回測歷史成交交割單主键集合
     * @return 结果
     */
    public int deleteTfTradeExecutionByIds(Long[] ids);

    /**
     * 删除回測歷史成交交割單信息
     * 
     * @param id 回測歷史成交交割單主键
     * @return 结果
     */
    public int deleteTfTradeExecutionById(Long id);
}
