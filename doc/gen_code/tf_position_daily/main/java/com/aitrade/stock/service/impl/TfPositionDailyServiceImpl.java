package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfPositionDailyMapper;
import com.aitrade.stock.domain.TfPositionDaily;
import com.aitrade.stock.service.ITfPositionDailyService;

/**
 * 回测持仓日历Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-12
 */
@Service
public class TfPositionDailyServiceImpl implements ITfPositionDailyService 
{
    @Autowired
    private TfPositionDailyMapper tfPositionDailyMapper;

    /**
     * 查询回测持仓日历
     * 
     * @param id 回测持仓日历主键
     * @return 回测持仓日历
     */
    @Override
    public TfPositionDaily selectTfPositionDailyById(Long id)
    {
        return tfPositionDailyMapper.selectTfPositionDailyById(id);
    }

    /**
     * 查询回测持仓日历列表
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 回测持仓日历
     */
    @Override
    public List<TfPositionDaily> selectTfPositionDailyList(TfPositionDaily tfPositionDaily)
    {
        return tfPositionDailyMapper.selectTfPositionDailyList(tfPositionDaily);
    }

    /**
     * 新增回测持仓日历
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 结果
     */
    @Override
    public int insertTfPositionDaily(TfPositionDaily tfPositionDaily)
    {
        tfPositionDaily.setCreateTime(DateUtils.getNowDate());
        return tfPositionDailyMapper.insertTfPositionDaily(tfPositionDaily);
    }

    /**
     * 修改回测持仓日历
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 结果
     */
    @Override
    public int updateTfPositionDaily(TfPositionDaily tfPositionDaily)
    {
        tfPositionDaily.setUpdateTime(DateUtils.getNowDate());
        return tfPositionDailyMapper.updateTfPositionDaily(tfPositionDaily);
    }

    /**
     * 批量删除回测持仓日历
     * 
     * @param ids 需要删除的回测持仓日历主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionDailyByIds(Long[] ids)
    {
        return tfPositionDailyMapper.deleteTfPositionDailyByIds(ids);
    }

    /**
     * 删除回测持仓日历信息
     * 
     * @param id 回测持仓日历主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionDailyById(Long id)
    {
        return tfPositionDailyMapper.deleteTfPositionDailyById(id);
    }
}
