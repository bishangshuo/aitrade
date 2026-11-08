package com.aitrade.stock.service;

import java.time.LocalDate;
import java.util.List;
import com.aitrade.stock.domain.TfPosition;

/**
 * 回測當前持倉Service接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface ITfPositionService 
{
    /**
     * 查询回測當前持倉
     * 
     * @param id 回測當前持倉主键
     * @return 回測當前持倉
     */
    public TfPosition selectTfPositionById(Long id);

    /**
     * 查询回測當前持倉列表
     * 
     * @param tfPosition 回測當前持倉
     * @return 回測當前持倉集合
     */
    public List<TfPosition> selectTfPositionList(TfPosition tfPosition);

    /**
     * 新增回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    public int insertTfPosition(TfPosition tfPosition);

    /**
     * 修改回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    public int updateTfPosition(TfPosition tfPosition);

    /**
     * 批量删除回測當前持倉
     * 
     * @param ids 需要删除的回測當前持倉主键集合
     * @return 结果
     */
    public int deleteTfPositionByIds(Long[] ids);

    /**
     * 删除回測當前持倉信息
     * 
     * @param id 回測當前持倉主键
     * @return 结果
     */
    public int deleteTfPositionById(Long id);

    /**
     * 检查持仓: 检查股票是否在持仓中
     * @param backtestId
     * @param symbol
     * @param tradeDate 交易日
     * @return
     */
    boolean checkInPosition(Long backtestId, String symbol, LocalDate tradeDate);
}
