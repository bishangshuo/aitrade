package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TaTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务规划Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-11
 */
@Mapper
public interface TaTaskMapper 
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

    public List<TaTask> getPendingTasks();

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
     * 删除任务规划
     * 
     * @param id 任务规划主键
     * @return 结果
     */
    public int deleteTaTaskById(Long id);

    /**
     * 批量删除任务规划
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTaTaskByIds(Long[] ids);
}
