package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TfConfigStrategyBottomBreakout;

/**
 * 底部反转策略回测参数配置Service接口
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public interface ITfConfigStrategyBottomBreakoutService 
{
    /**
     * 查询底部反转策略回测参数配置
     * 
     * @param id 底部反转策略回测参数配置主键
     * @return 底部反转策略回测参数配置
     */
    public TfConfigStrategyBottomBreakout selectTfConfigStrategyBottomBreakoutById(Long id);

    /**
     * 查询底部反转策略回测参数配置列表
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 底部反转策略回测参数配置集合
     */
    public List<TfConfigStrategyBottomBreakout> selectTfConfigStrategyBottomBreakoutList(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout);

    /**
     * 新增底部反转策略回测参数配置
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 结果
     */
    public int insertTfConfigStrategyBottomBreakout(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout);

    /**
     * 修改底部反转策略回测参数配置
     * 
     * @param tfConfigStrategyBottomBreakout 底部反转策略回测参数配置
     * @return 结果
     */
    public int updateTfConfigStrategyBottomBreakout(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout);

    /**
     * 批量删除底部反转策略回测参数配置
     * 
     * @param ids 需要删除的底部反转策略回测参数配置主键集合
     * @return 结果
     */
    public int deleteTfConfigStrategyBottomBreakoutByIds(Long[] ids);

    /**
     * 删除底部反转策略回测参数配置信息
     * 
     * @param id 底部反转策略回测参数配置主键
     * @return 结果
     */
    public int deleteTfConfigStrategyBottomBreakoutById(Long id);
}
