package com.aitrade.stock.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aitrade.common.annotation.Log;
import com.aitrade.common.core.controller.BaseController;
import com.aitrade.common.core.domain.AjaxResult;
import com.aitrade.common.enums.BusinessType;
import com.aitrade.stock.domain.TaTask;
import com.aitrade.stock.service.ITaTaskService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 任务规划Controller
 * 
 * @author aitrade
 * @date 2026-10-11
 */
@RestController
@RequestMapping("/stock/tatask")
public class TaTaskController extends BaseController
{
    @Autowired
    private ITaTaskService taTaskService;

    /**
     * 查询任务规划列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:list')")
    @GetMapping("/list")
    public TableDataInfo list(TaTask taTask)
    {
        startPage();
        List<TaTask> list = taTaskService.selectTaTaskList(taTask);
        return getDataTable(list);
    }

    /**
     * 导出任务规划列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:export')")
    @Log(title = "任务规划", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TaTask taTask)
    {
        List<TaTask> list = taTaskService.selectTaTaskList(taTask);
        ExcelUtil<TaTask> util = new ExcelUtil<TaTask>(TaTask.class);
        util.exportExcel(response, list, "任务规划数据");
    }

    /**
     * 获取任务规划详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(taTaskService.selectTaTaskById(id));
    }

    /**
     * 新增任务规划
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:add')")
    @Log(title = "任务规划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TaTask taTask)
    {
        return toAjax(taTaskService.insertTaTask(taTask));
    }

    /**
     * 修改任务规划
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:edit')")
    @Log(title = "任务规划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TaTask taTask)
    {
        return toAjax(taTaskService.updateTaTask(taTask));
    }

    /**
     * 删除任务规划
     */
    @PreAuthorize("@ss.hasPermi('stock:tatask:remove')")
    @Log(title = "任务规划", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(taTaskService.deleteTaTaskByIds(ids));
    }
}
