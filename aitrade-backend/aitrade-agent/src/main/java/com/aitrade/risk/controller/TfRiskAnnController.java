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
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 风险公告Controller
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@RestController
@RequestMapping("/risk/tfriskann")
public class TfRiskAnnController extends BaseController
{
    @Autowired
    private ITfRiskAnnService tfRiskAnnService;

    /**
     * 查询风险公告列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfRiskAnn tfRiskAnn)
    {
        startPage();
        List<TfRiskAnn> list = tfRiskAnnService.selectTfRiskAnnList(tfRiskAnn);
        return getDataTable(list);
    }

    /**
     * 导出风险公告列表
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:export')")
    @Log(title = "风险公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfRiskAnn tfRiskAnn)
    {
        List<TfRiskAnn> list = tfRiskAnnService.selectTfRiskAnnList(tfRiskAnn);
        ExcelUtil<TfRiskAnn> util = new ExcelUtil<TfRiskAnn>(TfRiskAnn.class);
        util.exportExcel(response, list, "风险公告数据");
    }

    /**
     * 获取风险公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfRiskAnnService.selectTfRiskAnnById(id));
    }

    /**
     * 新增风险公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:add')")
    @Log(title = "风险公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfRiskAnn tfRiskAnn)
    {
        return toAjax(tfRiskAnnService.insertTfRiskAnn(tfRiskAnn));
    }

    /**
     * 修改风险公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:edit')")
    @Log(title = "风险公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfRiskAnn tfRiskAnn)
    {
        return toAjax(tfRiskAnnService.updateTfRiskAnn(tfRiskAnn));
    }

    /**
     * 删除风险公告
     */
    @PreAuthorize("@ss.hasPermi('risk:tfriskann:remove')")
    @Log(title = "风险公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfRiskAnnService.deleteTfRiskAnnByIds(ids));
    }
}
