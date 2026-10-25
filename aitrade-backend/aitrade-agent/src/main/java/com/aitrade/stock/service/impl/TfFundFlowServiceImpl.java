package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfFundFlowMapper;
import com.aitrade.stock.domain.TfFundFlow;
import com.aitrade.stock.service.ITfFundFlowService;

/**
 * 回測資金流水明細Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Service
public class TfFundFlowServiceImpl implements ITfFundFlowService 
{
    @Autowired
    private TfFundFlowMapper tfFundFlowMapper;

    /**
     * 查询回測資金流水明細
     * 
     * @param id 回測資金流水明細主键
     * @return 回測資金流水明細
     */
    @Override
    public TfFundFlow selectTfFundFlowById(Long id)
    {
        return tfFundFlowMapper.selectTfFundFlowById(id);
    }

    /**
     * 查询回測資金流水明細列表
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 回測資金流水明細
     */
    @Override
    public List<TfFundFlow> selectTfFundFlowList(TfFundFlow tfFundFlow)
    {
        return tfFundFlowMapper.selectTfFundFlowList(tfFundFlow);
    }

    /**
     * 新增回測資金流水明細
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 结果
     */
    @Override
    public int insertTfFundFlow(TfFundFlow tfFundFlow)
    {
        tfFundFlow.setCreateTime(DateUtils.getNowDate());
        return tfFundFlowMapper.insertTfFundFlow(tfFundFlow);
    }

    /**
     * 修改回測資金流水明細
     * 
     * @param tfFundFlow 回測資金流水明細
     * @return 结果
     */
    @Override
    public int updateTfFundFlow(TfFundFlow tfFundFlow)
    {
        return tfFundFlowMapper.updateTfFundFlow(tfFundFlow);
    }

    /**
     * 批量删除回測資金流水明細
     * 
     * @param ids 需要删除的回測資金流水明細主键
     * @return 结果
     */
    @Override
    public int deleteTfFundFlowByIds(Long[] ids)
    {
        return tfFundFlowMapper.deleteTfFundFlowByIds(ids);
    }

    /**
     * 删除回測資金流水明細信息
     * 
     * @param id 回測資金流水明細主键
     * @return 结果
     */
    @Override
    public int deleteTfFundFlowById(Long id)
    {
        return tfFundFlowMapper.deleteTfFundFlowById(id);
    }
}
