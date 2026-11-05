package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TfConfigStrategyStrongBreakout;

/**
 * 主升浪策略回测参数配置Service接口
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public interface ITfConfigStrategyStrongBreakoutService 
{
    /**
     * 查询主升浪策略回测参数配置
     * 
     * @param id 主升浪策略回测参数配置主键
     * @return 主升浪策略回测参数配置
     */
    public TfConfigStrategyStrongBreakout selectTfConfigStrategyStrongBreakoutById(Long id);

    /**
     * 查询主升浪策略回测参数配置列表
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 主升浪策略回测参数配置集合
     */
    public List<TfConfigStrategyStrongBreakout> selectTfConfigStrategyStrongBreakoutList(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout);

    /**
     * 新增主升浪策略回测参数配置
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 结果
     */
    public int insertTfConfigStrategyStrongBreakout(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout);

    /**
     * 修改主升浪策略回测参数配置
     * 
     * @param tfConfigStrategyStrongBreakout 主升浪策略回测参数配置
     * @return 结果
     */
    public int updateTfConfigStrategyStrongBreakout(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout);

    /**
     * 批量删除主升浪策略回测参数配置
     * 
     * @param ids 需要删除的主升浪策略回测参数配置主键集合
     * @return 结果
     */
    public int deleteTfConfigStrategyStrongBreakoutByIds(Long[] ids);

    /**
     * 删除主升浪策略回测参数配置信息
     * 
     * @param id 主升浪策略回测参数配置主键
     * @return 结果
     */
    public int deleteTfConfigStrategyStrongBreakoutById(Long id);
}
