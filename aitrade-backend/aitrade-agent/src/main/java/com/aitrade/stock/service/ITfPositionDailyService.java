package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TfPositionDaily;

/**
 * 回测持仓日历Service接口
 * 
 * @author aitrade
 * @date 2026-11-12
 */
public interface ITfPositionDailyService 
{
    /**
     * 查询回测持仓日历
     * 
     * @param id 回测持仓日历主键
     * @return 回测持仓日历
     */
    public TfPositionDaily selectTfPositionDailyById(Long id);

    /**
     * 查询回测持仓日历列表
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 回测持仓日历集合
     */
    public List<TfPositionDaily> selectTfPositionDailyList(TfPositionDaily tfPositionDaily);

    /**
     * 新增回测持仓日历
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 结果
     */
    public int insertTfPositionDaily(TfPositionDaily tfPositionDaily);

    /**
     * 修改回测持仓日历
     * 
     * @param tfPositionDaily 回测持仓日历
     * @return 结果
     */
    public int updateTfPositionDaily(TfPositionDaily tfPositionDaily);

    /**
     * 批量删除回测持仓日历
     * 
     * @param ids 需要删除的回测持仓日历主键集合
     * @return 结果
     */
    public int deleteTfPositionDailyByIds(Long[] ids);

    /**
     * 删除回测持仓日历信息
     * 
     * @param id 回测持仓日历主键
     * @return 结果
     */
    public int deleteTfPositionDailyById(Long id);
}
