package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TaTaskMapper;
import com.aitrade.stock.domain.TaTask;
import com.aitrade.stock.service.ITaTaskService;

/**
 * 任务规划Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-11
 */
@Service
public class TaTaskServiceImpl implements ITaTaskService 
{
    @Autowired
    private TaTaskMapper taTaskMapper;

    /**
     * 查询任务规划
     * 
     * @param id 任务规划主键
     * @return 任务规划
     */
    @Override
    public TaTask selectTaTaskById(Long id)
    {
        return taTaskMapper.selectTaTaskById(id);
    }

    /**
     * 查询任务规划列表
     * 
     * @param taTask 任务规划
     * @return 任务规划
     */
    @Override
    public List<TaTask> selectTaTaskList(TaTask taTask)
    {
        return taTaskMapper.selectTaTaskList(taTask);
    }

    /**
     * 新增任务规划
     * 
     * @param taTask 任务规划
     * @return 结果
     */
    @Override
    public int insertTaTask(TaTask taTask)
    {
        taTask.setCreateTime(DateUtils.getNowDate());
        return taTaskMapper.insertTaTask(taTask);
    }

    /**
     * 修改任务规划
     * 
     * @param taTask 任务规划
     * @return 结果
     */
    @Override
    public int updateTaTask(TaTask taTask)
    {
        taTask.setUpdateTime(DateUtils.getNowDate());
        return taTaskMapper.updateTaTask(taTask);
    }

    /**
     * 批量删除任务规划
     * 
     * @param ids 需要删除的任务规划主键
     * @return 结果
     */
    @Override
    public int deleteTaTaskByIds(Long[] ids)
    {
        return taTaskMapper.deleteTaTaskByIds(ids);
    }

    /**
     * 删除任务规划信息
     * 
     * @param id 任务规划主键
     * @return 结果
     */
    @Override
    public int deleteTaTaskById(Long id)
    {
        return taTaskMapper.deleteTaTaskById(id);
    }
}
