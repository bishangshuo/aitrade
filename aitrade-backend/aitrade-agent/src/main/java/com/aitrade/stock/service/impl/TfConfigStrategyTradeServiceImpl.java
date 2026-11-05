package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfConfigStrategyTradeMapper;
import com.aitrade.stock.domain.TfConfigStrategyTrade;
import com.aitrade.stock.service.ITfConfigStrategyTradeService;

/**
 * 交易策略参数配置Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@Service
public class TfConfigStrategyTradeServiceImpl implements ITfConfigStrategyTradeService 
{
    @Autowired
    private TfConfigStrategyTradeMapper tfConfigStrategyTradeMapper;

    /**
     * 查询交易策略参数配置
     * 
     * @param id 交易策略参数配置主键
     * @return 交易策略参数配置
     */
    @Override
    public TfConfigStrategyTrade selectTfConfigStrategyTradeById(Long id)
    {
        return tfConfigStrategyTradeMapper.selectTfConfigStrategyTradeById(id);
    }

    /**
     * 查询交易策略参数配置列表
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 交易策略参数配置
     */
    @Override
    public List<TfConfigStrategyTrade> selectTfConfigStrategyTradeList(TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        return tfConfigStrategyTradeMapper.selectTfConfigStrategyTradeList(tfConfigStrategyTrade);
    }

    /**
     * 新增交易策略参数配置
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 结果
     */
    @Override
    public int insertTfConfigStrategyTrade(TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        tfConfigStrategyTrade.setCreateTime(DateUtils.getNowDate());
        return tfConfigStrategyTradeMapper.insertTfConfigStrategyTrade(tfConfigStrategyTrade);
    }

    /**
     * 修改交易策略参数配置
     * 
     * @param tfConfigStrategyTrade 交易策略参数配置
     * @return 结果
     */
    @Override
    public int updateTfConfigStrategyTrade(TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        tfConfigStrategyTrade.setUpdateTime(DateUtils.getNowDate());
        return tfConfigStrategyTradeMapper.updateTfConfigStrategyTrade(tfConfigStrategyTrade);
    }

    /**
     * 批量删除交易策略参数配置
     * 
     * @param ids 需要删除的交易策略参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyTradeByIds(Long[] ids)
    {
        return tfConfigStrategyTradeMapper.deleteTfConfigStrategyTradeByIds(ids);
    }

    /**
     * 删除交易策略参数配置信息
     * 
     * @param id 交易策略参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyTradeById(Long id)
    {
        return tfConfigStrategyTradeMapper.deleteTfConfigStrategyTradeById(id);
    }
}
