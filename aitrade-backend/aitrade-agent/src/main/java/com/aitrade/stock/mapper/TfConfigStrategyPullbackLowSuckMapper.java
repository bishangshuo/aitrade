package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfConfigStrategyPullbackLowSuck;

/**
 * 震荡攀升低吸策略回测参数配置Mapper接口
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public interface TfConfigStrategyPullbackLowSuckMapper 
{
    /**
     * 查询震荡攀升低吸策略回测参数配置
     * 
     * @param id 震荡攀升低吸策略回测参数配置主键
     * @return 震荡攀升低吸策略回测参数配置
     */
    public TfConfigStrategyPullbackLowSuck selectTfConfigStrategyPullbackLowSuckById(Long id);

    /**
     * 查询震荡攀升低吸策略回测参数配置列表
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 震荡攀升低吸策略回测参数配置集合
     */
    public List<TfConfigStrategyPullbackLowSuck> selectTfConfigStrategyPullbackLowSuckList(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck);

    /**
     * 新增震荡攀升低吸策略回测参数配置
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 结果
     */
    public int insertTfConfigStrategyPullbackLowSuck(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck);

    /**
     * 修改震荡攀升低吸策略回测参数配置
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 结果
     */
    public int updateTfConfigStrategyPullbackLowSuck(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck);

    /**
     * 删除震荡攀升低吸策略回测参数配置
     * 
     * @param id 震荡攀升低吸策略回测参数配置主键
     * @return 结果
     */
    public int deleteTfConfigStrategyPullbackLowSuckById(Long id);

    /**
     * 批量删除震荡攀升低吸策略回测参数配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfConfigStrategyPullbackLowSuckByIds(Long[] ids);
}
