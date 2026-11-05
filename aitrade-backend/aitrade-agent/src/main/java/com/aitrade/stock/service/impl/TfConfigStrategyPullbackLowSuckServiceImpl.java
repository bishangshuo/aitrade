package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfConfigStrategyPullbackLowSuckMapper;
import com.aitrade.stock.domain.TfConfigStrategyPullbackLowSuck;
import com.aitrade.stock.service.ITfConfigStrategyPullbackLowSuckService;

/**
 * 震荡攀升低吸策略回测参数配置Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@Service
public class TfConfigStrategyPullbackLowSuckServiceImpl implements ITfConfigStrategyPullbackLowSuckService 
{
    @Autowired
    private TfConfigStrategyPullbackLowSuckMapper tfConfigStrategyPullbackLowSuckMapper;

    /**
     * 查询震荡攀升低吸策略回测参数配置
     * 
     * @param id 震荡攀升低吸策略回测参数配置主键
     * @return 震荡攀升低吸策略回测参数配置
     */
    @Override
    public TfConfigStrategyPullbackLowSuck selectTfConfigStrategyPullbackLowSuckById(Long id)
    {
        return tfConfigStrategyPullbackLowSuckMapper.selectTfConfigStrategyPullbackLowSuckById(id);
    }

    /**
     * 查询震荡攀升低吸策略回测参数配置列表
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 震荡攀升低吸策略回测参数配置
     */
    @Override
    public List<TfConfigStrategyPullbackLowSuck> selectTfConfigStrategyPullbackLowSuckList(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        return tfConfigStrategyPullbackLowSuckMapper.selectTfConfigStrategyPullbackLowSuckList(tfConfigStrategyPullbackLowSuck);
    }

    /**
     * 新增震荡攀升低吸策略回测参数配置
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 结果
     */
    @Override
    public int insertTfConfigStrategyPullbackLowSuck(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        tfConfigStrategyPullbackLowSuck.setCreateTime(DateUtils.getNowDate());
        return tfConfigStrategyPullbackLowSuckMapper.insertTfConfigStrategyPullbackLowSuck(tfConfigStrategyPullbackLowSuck);
    }

    /**
     * 修改震荡攀升低吸策略回测参数配置
     * 
     * @param tfConfigStrategyPullbackLowSuck 震荡攀升低吸策略回测参数配置
     * @return 结果
     */
    @Override
    public int updateTfConfigStrategyPullbackLowSuck(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        return tfConfigStrategyPullbackLowSuckMapper.updateTfConfigStrategyPullbackLowSuck(tfConfigStrategyPullbackLowSuck);
    }

    /**
     * 批量删除震荡攀升低吸策略回测参数配置
     * 
     * @param ids 需要删除的震荡攀升低吸策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyPullbackLowSuckByIds(Long[] ids)
    {
        return tfConfigStrategyPullbackLowSuckMapper.deleteTfConfigStrategyPullbackLowSuckByIds(ids);
    }

    /**
     * 删除震荡攀升低吸策略回测参数配置信息
     * 
     * @param id 震荡攀升低吸策略回测参数配置主键
     * @return 结果
     */
    @Override
    public int deleteTfConfigStrategyPullbackLowSuckById(Long id)
    {
        return tfConfigStrategyPullbackLowSuckMapper.deleteTfConfigStrategyPullbackLowSuckById(id);
    }
}
