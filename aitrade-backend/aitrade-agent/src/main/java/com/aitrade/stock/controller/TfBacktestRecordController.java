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
import com.aitrade.stock.domain.TfBacktestRecord;
import com.aitrade.stock.service.ITfBacktestRecordService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回测记录Controller
 * 
 * @author aitrade
 * @date 2026-11-05
 */
@RestController
@RequestMapping("/stock/bucktestrecord")
public class TfBacktestRecordController extends BaseController
{
    @Autowired
    private ITfBacktestRecordService tfBacktestRecordService;

    /**
     * 查询回测记录列表
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfBacktestRecord tfBacktestRecord)
    {
        startPage();
        List<TfBacktestRecord> list = tfBacktestRecordService.selectTfBacktestRecordList(tfBacktestRecord);
        return getDataTable(list);
    }

    /**
     * 导出回测记录列表
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:export')")
    @Log(title = "回测记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfBacktestRecord tfBacktestRecord)
    {
        List<TfBacktestRecord> list = tfBacktestRecordService.selectTfBacktestRecordList(tfBacktestRecord);
        ExcelUtil<TfBacktestRecord> util = new ExcelUtil<TfBacktestRecord>(TfBacktestRecord.class);
        util.exportExcel(response, list, "回测记录数据");
    }

    /**
     * 获取回测记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfBacktestRecordService.selectTfBacktestRecordById(id));
    }

    /**
     * 新增回测记录
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:add')")
    @Log(title = "回测记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfBacktestRecord tfBacktestRecord)
    {
        return toAjax(tfBacktestRecordService.insertTfBacktestRecord(tfBacktestRecord));
    }

    /**
     * 修改回测记录
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:edit')")
    @Log(title = "回测记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfBacktestRecord tfBacktestRecord)
    {
        return toAjax(tfBacktestRecordService.updateTfBacktestRecord(tfBacktestRecord));
    }

    /**
     * 删除回测记录
     */
    @PreAuthorize("@ss.hasPermi('stock:bucktestrecord:remove')")
    @Log(title = "回测记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfBacktestRecordService.deleteTfBacktestRecordByIds(ids));
    }
}
