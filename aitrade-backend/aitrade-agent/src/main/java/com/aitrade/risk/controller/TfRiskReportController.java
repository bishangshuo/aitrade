package com.aitrade.risk.controller;

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
import com.aitrade.risk.domain.TfRiskReport;
import com.aitrade.risk.service.ITfRiskReportService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * AI研报Controller
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@RestController
@RequestMapping("/risk/tfriskreport")
public class TfRiskReportController extends BaseController
{
    @Autowired
    private ITfRiskReportService tfRiskReportService;

    /**
     * 查询AI研报列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfRiskReport tfRiskReport)
    {
        startPage();
        List<TfRiskReport> list = tfRiskReportService.selectTfRiskReportList(tfRiskReport);
        return getDataTable(list);
    }

    /**
     * 导出AI研报列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:export')")
    @Log(title = "AI研报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfRiskReport tfRiskReport)
    {
        List<TfRiskReport> list = tfRiskReportService.selectTfRiskReportList(tfRiskReport);
        ExcelUtil<TfRiskReport> util = new ExcelUtil<TfRiskReport>(TfRiskReport.class);
        util.exportExcel(response, list, "AI研报数据");
    }

    /**
     * 获取AI研报详细信息
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfRiskReportService.selectTfRiskReportById(id));
    }

    /**
     * 新增AI研报
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:add')")
    @Log(title = "AI研报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfRiskReport tfRiskReport)
    {
        return toAjax(tfRiskReportService.insertTfRiskReport(tfRiskReport));
    }

    /**
     * 修改AI研报
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:edit')")
    @Log(title = "AI研报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfRiskReport tfRiskReport)
    {
        return toAjax(tfRiskReportService.updateTfRiskReport(tfRiskReport));
    }

    /**
     * 删除AI研报
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreport:remove')")
    @Log(title = "AI研报", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfRiskReportService.deleteTfRiskReportByIds(ids));
    }
}
