package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TfFundFlow;

/**
 * 回測資金流水明細Service接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface ITfFundFlowService 
{
    /**
     * 查询回測資金流水明細
     * 
     * @param id 回測資金流水明細主键
     * @return 回測資金流水明細
     */
    public TfFundFlow selectTfFundFlowById(Long id);

    /**
     * 查询回測資金流水明細列表
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 回測資金流水明細集合
     */
    public List<TfFundFlow> selectTfFundFlowList(TfFundFlow tfFundFlow);

    /**
     * 新增回測資金流水明細
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 结果
     */
    public int insertTfFundFlow(TfFundFlow tfFundFlow);

    /**
     * 修改回測資金流水明細
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 结果
     */
    public int updateTfFundFlow(TfFundFlow tfFundFlow);

    /**
     * 批量删除回測資金流水明細
     * 
     * @param ids 需要删除的回測資金流水明細主键集合
     * @return 结果
     */
    public int deleteTfFundFlowByIds(Long[] ids);

    /**
     * 删除回測資金流水明細信息
     * 
     * @param id 回測資金流水明細主键
     * @return 结果
     */
    public int deleteTfFundFlowById(Long id);
}
