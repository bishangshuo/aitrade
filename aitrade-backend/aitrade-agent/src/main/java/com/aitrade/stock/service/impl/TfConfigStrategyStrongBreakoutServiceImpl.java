package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfConfigStrategyStrongBreakoutMapper;
import com.aitrade.stock.domain.TfConfigStrategyStrongBreakout;
import com.aitrade.stock.service.ITfConfigStrategyStrongBreakoutService;

/**
 * 主升浪策略回测参数配置Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@Service
public class TfConfigStrategyStrongBreakoutServiceImpl implements ITfConfigStrategyStrongBreakoutService 
{
    @Autowired
    private TfConfigStrategyStrongBreakoutMapper tfConfigStrategyStrongBreakoutMapper;

    /**
     * 查询主升浪策略回测参数配置
     * 
     * @param id 主升浪策略回测参数配置主键
     * @return 主升浪策略回测参数配置
     */
    @Override
    public TfConfigStrategyStrongBreakout selectTfConfigStrategyStrongBreakoutById(Long id)
    {
        return tfConfigStrategyStrongBreakoutMapper.selectTfConfigStrategyStrongBreakoutById(id);
    }

    /**
     * 查询主升浪策略回测参数配置列表
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 主升浪策略回测参数配置
     */
    @Override
    public List<TfConfigStrategyStrongBreakout> selectTfConfigStrategyStrongBreakoutList(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        return tfConfigStrategyStrongBreakoutMapper.selectTfConfigStrategyStrongBreakoutList(tfConfigStrategyStrongBreakout);
    }

    /**
     * 新增主升浪策略回测参数配置
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 结果
     */
    @Override
    public int insertTfConfigStrategyStrongBreakout(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        tfConfigStrategyStrongBreakout.setCreateTime(DateUtils.getNowDate());
        return tfConfigStrategyStrongBreakoutMapper.insertTfConfigStrategyStrongBreakout(tfConfigStrategyStrongBreakout);
    }

    /**
     * 修改主升浪策略回测参数配置
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 结果
     */
    @Override
    public int updateTfConfigStrategyStrongBreakout(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        return tfConfigStrategyStrongBreakoutMapper.updateTfConfigStrategyStrongBreakout(tfConfigStrategyStrongBreakout);
    }

    /**
     * 批量删除主升浪策略回测参数配置
     * 
     * @param ids 需要删除的主升浪策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyStrongBreakoutByIds(Long[] ids)
    {
        return tfConfigStrategyStrongBreakoutMapper.deleteTfConfigStrategyStrongBreakoutByIds(ids);
    }

    /**
     * 删除主升浪策略回测参数配置信息
     * 
     * @param id 主升浪策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyStrongBreakoutById(Long id)
    {
        return tfConfigStrategyStrongBreakoutMapper.deleteTfConfigStrategyStrongBreakoutById(id);
    }
}
