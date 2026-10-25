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
import com.aitrade.stock.domain.TfTradeExecution;
import com.aitrade.stock.service.ITfTradeExecutionService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回測歷史成交交割單Controller
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@RestController
@RequestMapping("/stock/tftradeexecution")
public class TfTradeExecutionController extends BaseController
{
    @Autowired
    private ITfTradeExecutionService tfTradeExecutionService;

    /**
     * 查询回測歷史成交交割單列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfTradeExecution tfTradeExecution)
    {
        startPage();
        List<TfTradeExecution> list = tfTradeExecutionService.selectTfTradeExecutionList(tfTradeExecution);
        return getDataTable(list);
    }

    /**
     * 导出回測歷史成交交割單列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:export')")
    @Log(title = "回測歷史成交交割單", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfTradeExecution tfTradeExecution)
    {
        List<TfTradeExecution> list = tfTradeExecutionService.selectTfTradeExecutionList(tfTradeExecution);
        ExcelUtil<TfTradeExecution> util = new ExcelUtil<TfTradeExecution>(TfTradeExecution.class);
        util.exportExcel(response, list, "回測歷史成交交割單数据");
    }

    /**
     * 获取回測歷史成交交割單详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfTradeExecutionService.selectTfTradeExecutionById(id));
    }

    /**
     * 新增回測歷史成交交割單
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:add')")
    @Log(title = "回測歷史成交交割單", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfTradeExecution tfTradeExecution)
    {
        return toAjax(tfTradeExecutionService.insertTfTradeExecution(tfTradeExecution));
    }

    /**
     * 修改回測歷史成交交割單
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:edit')")
    @Log(title = "回測歷史成交交割單", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfTradeExecution tfTradeExecution)
    {
        return toAjax(tfTradeExecutionService.updateTfTradeExecution(tfTradeExecution));
    }

    /**
     * 删除回測歷史成交交割單
     */
    @PreAuthorize("@ss.hasPermi('stock:tftradeexecution:remove')")
    @Log(title = "回測歷史成交交割單", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfTradeExecutionService.deleteTfTradeExecutionByIds(ids));
    }
}
