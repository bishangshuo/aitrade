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
import com.aitrade.stock.domain.TfFundFlow;
import com.aitrade.stock.service.ITfFundFlowService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回測資金流水明細Controller
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@RestController
@RequestMapping("/stock/tffundflow")
public class TfFundFlowController extends BaseController
{
    @Autowired
    private ITfFundFlowService tfFundFlowService;

    /**
     * 查询回測資金流水明細列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfFundFlow tfFundFlow)
    {
        startPage();
        List<TfFundFlow> list = tfFundFlowService.selectTfFundFlowList(tfFundFlow);
        return getDataTable(list);
    }

    /**
     * 导出回測資金流水明細列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:export')")
    @Log(title = "回測資金流水明細", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfFundFlow tfFundFlow)
    {
        List<TfFundFlow> list = tfFundFlowService.selectTfFundFlowList(tfFundFlow);
        ExcelUtil<TfFundFlow> util = new ExcelUtil<TfFundFlow>(TfFundFlow.class);
        util.exportExcel(response, list, "回測資金流水明細数据");
    }

    /**
     * 获取回測資金流水明細详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfFundFlowService.selectTfFundFlowById(id));
    }

    /**
     * 新增回測資金流水明細
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:add')")
    @Log(title = "回測資金流水明細", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfFundFlow tfFundFlow)
    {
        return toAjax(tfFundFlowService.insertTfFundFlow(tfFundFlow));
    }

    /**
     * 修改回測資金流水明細
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:edit')")
    @Log(title = "回測資金流水明細", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfFundFlow tfFundFlow)
    {
        return toAjax(tfFundFlowService.updateTfFundFlow(tfFundFlow));
    }

    /**
     * 删除回測資金流水明細
     */
    @PreAuthorize("@ss.hasPermi('stock:tffundflow:remove')")
    @Log(title = "回測資金流水明細", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfFundFlowService.deleteTfFundFlowByIds(ids));
    }
}
