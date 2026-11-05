package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfConfigStrategyTrade;

/**
 * 交易策略参数配置Mapper接口
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public interface TfConfigStrategyTradeMapper 
{
    /**
     * 查询交易策略参数配置
     * 
     * @param id 交易策略参数配置主键
     * @return 交易策略参数配置
     */
    public TfConfigStrategyTrade selectTfConfigStrategyTradeById(Long id);

    /**
     * 查询交易策略参数配置列表
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 交易策略参数配置集合
     */
    public List<TfConfigStrategyTrade> selectTfConfigStrategyTradeList(TfConfigStrategyTrade tfConfigStrategyTrade);

    /**
     * 新增交易策略参数配置
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 结果
     */
    public int insertTfConfigStrategyTrade(TfConfigStrategyTrade tfConfigStrategyTrade);

    /**
     * 修改交易策略参数配置
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 结果
     */
    public int updateTfConfigStrategyTrade(TfConfigStrategyTrade tfConfigStrategyTrade);

    /**
     * 删除交易策略参数配置
     * 
     * @param id 交易策略参数配置主键
     * @return 结果
     */
    public int deleteTfConfigStrategyTradeById(Long id);

    /**
     * 批量删除交易策略参数配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfConfigStrategyTradeByIds(Long[] ids);
}
