package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfBacktestRecord;

/**
 * 回测记录Mapper接口
 * 
 * @author aitrade
 * @date 2026-11-05
 */
public interface TfBacktestRecordMapper 
{
    /**
     * 查询回测记录
     * 
     * @param id 回测记录主键
     * @return 回测记录
     */
    public TfBacktestRecord selectTfBacktestRecordById(Long id);

    /**
     * 查询回测记录列表
     * 
     * @param tfBacktestRecord 回测记录
     * @return 回测记录集合
     */
    public List<TfBacktestRecord> selectTfBacktestRecordList(TfBacktestRecord tfBacktestRecord);

    /**
     * 新增回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    public int insertTfBacktestRecord(TfBacktestRecord tfBacktestRecord);

    /**
     * 修改回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    public int updateTfBacktestRecord(TfBacktestRecord tfBacktestRecord);

    /**
     * 删除回测记录
     * 
     * @param id 回测记录主键
     * @return 结果
     */
    public int deleteTfBacktestRecordById(Long id);

    /**
     * 批量删除回测记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfBacktestRecordByIds(Long[] ids);
}
