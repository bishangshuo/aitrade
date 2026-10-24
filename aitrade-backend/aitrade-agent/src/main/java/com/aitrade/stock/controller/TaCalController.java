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
import com.aitrade.stock.domain.TaCal;
import com.aitrade.stock.service.ITaCalService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 交易日历Controller
 * 
 * @author bishangshuo
 * @date 2026-10-19
 */
@RestController
@RequestMapping("/stock/tacal")
public class TaCalController extends BaseController
{
    @Autowired
    private ITaCalService taCalService;

    /**
     * 查询交易日历列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:list')")
    @GetMapping("/list")
    public TableDataInfo list(TaCal taCal)
    {
        startPage();
        List<TaCal> list = taCalService.selectTaCalList(taCal);
        return getDataTable(list);
    }

    /**
     * 导出交易日历列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:export')")
    @Log(title = "交易日历", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TaCal taCal)
    {
        List<TaCal> list = taCalService.selectTaCalList(taCal);
        ExcelUtil<TaCal> util = new ExcelUtil<TaCal>(TaCal.class);
        util.exportExcel(response, list, "交易日历数据");
    }

    /**
     * 获取交易日历详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(taCalService.selectTaCalById(id));
    }

    /**
     * 新增交易日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:add')")
    @Log(title = "交易日历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TaCal taCal)
    {
        return toAjax(taCalService.insertTaCal(taCal));
    }

    /**
     * 修改交易日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:edit')")
    @Log(title = "交易日历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TaCal taCal)
    {
        return toAjax(taCalService.updateTaCal(taCal));
    }

    /**
     * 删除交易日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tacal:remove')")
    @Log(title = "交易日历", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(taCalService.deleteTaCalByIds(ids));
    }
}
