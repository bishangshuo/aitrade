package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfConfigStrategyOversoldReboundMapper;
import com.aitrade.stock.domain.TfConfigStrategyOversoldRebound;
import com.aitrade.stock.service.ITfConfigStrategyOversoldReboundService;

/**
 * 超跌反弹策略回测参数配置Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@Service
public class TfConfigStrategyOversoldReboundServiceImpl implements ITfConfigStrategyOversoldReboundService 
{
    @Autowired
    private TfConfigStrategyOversoldReboundMapper tfConfigStrategyOversoldReboundMapper;

    /**
     * 查询超跌反弹策略回测参数配置
     * 
     * @param id 超跌反弹策略回测参数配置主键
     * @return 超跌反弹策略回测参数配置
     */
    @Override
    public TfConfigStrategyOversoldRebound selectTfConfigStrategyOversoldReboundById(Long id)
    {
        return tfConfigStrategyOversoldReboundMapper.selectTfConfigStrategyOversoldReboundById(id);
    }

    /**
     * 查询超跌反弹策略回测参数配置列表
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 超跌反弹策略回测参数配置
     */
    @Override
    public List<TfConfigStrategyOversoldRebound> selectTfConfigStrategyOversoldReboundList(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        return tfConfigStrategyOversoldReboundMapper.selectTfConfigStrategyOversoldReboundList(tfConfigStrategyOversoldRebound);
    }

    /**
     * 新增超跌反弹策略回测参数配置
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 结果
     */
    @Override
    public int insertTfConfigStrategyOversoldRebound(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        tfConfigStrategyOversoldRebound.setCreateTime(DateUtils.getNowDate());
        return tfConfigStrategyOversoldReboundMapper.insertTfConfigStrategyOversoldRebound(tfConfigStrategyOversoldRebound);
    }

    /**
     * 修改超跌反弹策略回测参数配置
     * 
     * @param tfConfigStrategyOversoldRebound 超跌反弹策略回测参数配置
     * @return 结果
     */
    @Override
    public int updateTfConfigStrategyOversoldRebound(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        return tfConfigStrategyOversoldReboundMapper.updateTfConfigStrategyOversoldRebound(tfConfigStrategyOversoldRebound);
    }

    /**
     * 批量删除超跌反弹策略回测参数配置
     * 
     * @param ids 需要删除的超跌反弹策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyOversoldReboundByIds(Long[] ids)
    {
        return tfConfigStrategyOversoldReboundMapper.deleteTfConfigStrategyOversoldReboundByIds(ids);
    }

    /**
     * 删除超跌反弹策略回测参数配置信息
     * 
     * @param id 超跌反弹策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyOversoldReboundById(Long id)
    {
        return tfConfigStrategyOversoldReboundMapper.deleteTfConfigStrategyOversoldReboundById(id);
    }
}
