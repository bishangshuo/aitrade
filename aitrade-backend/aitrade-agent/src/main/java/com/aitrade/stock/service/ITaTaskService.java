package com.aitrade.stock.service;

import java.util.List;
import com.aitrade.stock.domain.TaTask;

/**
 * 任务规划Service接口
 * 
 * @author aitrade
 * @date 2026-10-11
 */
public interface ITaTaskService 
{
    /**
     * 查询任务规划
     * 
     * @param id 任务规划主键
     * @return 任务规划
     */
    public TaTask selectTaTaskById(Long id);

    /**
     * 查询任务规划列表
     * 
     * @param taTask 任务规划
     * @return 任务规划集合
     */
    public List<TaTask> selectTaTaskList(TaTask taTask);

    /**
     * 新增任务规划
     * 
     * @param taTask 任务规划
     * @return 结果
     */
    public int insertTaTask(TaTask taTask);

    /**
     * 修改任务规划
     * 
     * @param taTask 任务规划
     * @return 结果
     */
    public int updateTaTask(TaTask taTask);

    /**
     * 批量删除任务规划
     * 
     * @param ids 需要删除的任务规划主键集合
     * @return 结果
     */
    public int deleteTaTaskByIds(Long[] ids);

    /**
     * 删除任务规划信息
     * 
     * @param id 任务规划主键
     * @return 结果
     */
    public int deleteTaTaskById(Long id);
}
