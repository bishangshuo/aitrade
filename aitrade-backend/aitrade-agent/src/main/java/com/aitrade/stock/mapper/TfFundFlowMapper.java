package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfFundFlow;

/**
 * 回測資金流水明細Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface TfFundFlowMapper 
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
     * 删除回測資金流水明細
     * 
     * @param id 回測資金流水明細主键
     * @return 结果
     */
    public int deleteTfFundFlowById(Long id);

    /**
     * 批量删除回測資金流水明細
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfFundFlowByIds(Long[] ids);
}
