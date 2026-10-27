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
import com.aitrade.risk.domain.TfRiskReportAnn;
import com.aitrade.risk.service.ITfRiskReportAnnService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 研报公告Controller
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@RestController
@RequestMapping("/risk/tfriskreportann")
public class TfRiskReportAnnController extends BaseController
{
    @Autowired
    private ITfRiskReportAnnService tfRiskReportAnnService;

    /**
     * 查询研报公告列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfRiskReportAnn tfRiskReportAnn)
    {
        startPage();
        List<TfRiskReportAnn> list = tfRiskReportAnnService.selectTfRiskReportAnnList(tfRiskReportAnn);
        return getDataTable(list);
    }

    /**
     * 导出研报公告列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:export')")
    @Log(title = "研报公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfRiskReportAnn tfRiskReportAnn)
    {
        List<TfRiskReportAnn> list = tfRiskReportAnnService.selectTfRiskReportAnnList(tfRiskReportAnn);
        ExcelUtil<TfRiskReportAnn> util = new ExcelUtil<TfRiskReportAnn>(TfRiskReportAnn.class);
        util.exportExcel(response, list, "研报公告数据");
    }

    /**
     * 获取研报公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfRiskReportAnnService.selectTfRiskReportAnnById(id));
    }

    /**
     * 新增研报公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:add')")
    @Log(title = "研报公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfRiskReportAnn tfRiskReportAnn)
    {
        return toAjax(tfRiskReportAnnService.insertTfRiskReportAnn(tfRiskReportAnn));
    }

    /**
     * 修改研报公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:edit')")
    @Log(title = "研报公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfRiskReportAnn tfRiskReportAnn)
    {
        return toAjax(tfRiskReportAnnService.updateTfRiskReportAnn(tfRiskReportAnn));
    }

    /**
     * 删除研报公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskreportann:remove')")
    @Log(title = "研报公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfRiskReportAnnService.deleteTfRiskReportAnnByIds(ids));
    }
}
