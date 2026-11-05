package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfConfigStrategyBottomBreakoutMapper;
import com.aitrade.stock.domain.TfConfigStrategyBottomBreakout;
import com.aitrade.stock.service.ITfConfigStrategyBottomBreakoutService;

/**
 * 底部反转策略回测参数配置Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@Service
public class TfConfigStrategyBottomBreakoutServiceImpl implements ITfConfigStrategyBottomBreakoutService 
{
    @Autowired
    private TfConfigStrategyBottomBreakoutMapper tfConfigStrategyBottomBreakoutMapper;

    /**
     * 查询底部反转策略回测参数配置
     * 
     * @param id 底部反转策略回测参数配置主键
     * @return 底部反转策略回测参数配置
     */
    @Override
    public TfConfigStrategyBottomBreakout selectTfConfigStrategyBottomBreakoutById(Long id)
    {
        return tfConfigStrategyBottomBreakoutMapper.selectTfConfigStrategyBottomBreakoutById(id);
    }

    /**
     * 查询底部反转策略回测参数配置列表
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 底部反转策略回测参数配置
     */
    @Override
    public List<TfConfigStrategyBottomBreakout> selectTfConfigStrategyBottomBreakoutList(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        return tfConfigStrategyBottomBreakoutMapper.selectTfConfigStrategyBottomBreakoutList(tfConfigStrategyBottomBreakout);
    }

    /**
     * 新增底部反转策略回测参数配置
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 结果
     */
    @Override
    public int insertTfConfigStrategyBottomBreakout(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        tfConfigStrategyBottomBreakout.setCreateTime(DateUtils.getNowDate());
        return tfConfigStrategyBottomBreakoutMapper.insertTfConfigStrategyBottomBreakout(tfConfigStrategyBottomBreakout);
    }

    /**
     * 修改底部反转策略回测参数配置
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 结果
     */
    @Override
    public int updateTfConfigStrategyBottomBreakout(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        return tfConfigStrategyBottomBreakoutMapper.updateTfConfigStrategyBottomBreakout(tfConfigStrategyBottomBreakout);
    }

    /**
     * 批量删除底部反转策略回测参数配置
     * 
     * @param ids 需要删除的底部反转策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyBottomBreakoutByIds(Long[] ids)
    {
        return tfConfigStrategyBottomBreakoutMapper.deleteTfConfigStrategyBottomBreakoutByIds(ids);
    }

    /**
     * 删除底部反转策略回测参数配置信息
     * 
     * @param id 底部反转策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyBottomBreakoutById(Long id)
    {
        return tfConfigStrategyBottomBreakoutMapper.deleteTfConfigStrategyBottomBreakoutById(id);
    }
}
