package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfConfigStrategyOversoldRebound;

/**
 * 超跌反弹策略回测参数配置Mapper接口
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public interface TfConfigStrategyOversoldReboundMapper 
{
    /**
     * 查询超跌反弹策略回测参数配置
     * 
     * @param id 超跌反弹策略回测参数配置主键
     * @return 超跌反弹策略回测参数配置
     */
    public TfConfigStrategyOversoldRebound selectTfConfigStrategyOversoldReboundById(Long id);

    /**
     * 查询超跌反弹策略回测参数配置列表
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 超跌反弹策略回测参数配置集合
     */
    public List<TfConfigStrategyOversoldRebound> selectTfConfigStrategyOversoldReboundList(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound);

    /**
     * 新增超跌反弹策略回测参数配置
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 结果
     */
    public int insertTfConfigStrategyOversoldRebound(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound);

    /**
     * 修改超跌反弹策略回测参数配置
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 结果
     */
    public int updateTfConfigStrategyOversoldRebound(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound);

    /**
     * 删除超跌反弹策略回测参数配置
     * 
     * @param id 超跌反弹策略回测参数配置主键
     * @return 结果
     */
    public int deleteTfConfigStrategyOversoldReboundById(Long id);

    /**
     * 批量删除超跌反弹策略回测参数配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfConfigStrategyOversoldReboundByIds(Long[] ids);
}
