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
import com.aitrade.stock.domain.TfConfigStrategyBottomBreakout;
import com.aitrade.stock.service.ITfConfigStrategyBottomBreakoutService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 底部反转策略回测参数配置Controller
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/stock/bottombreakoutconfig")
public class TfConfigStrategyBottomBreakoutController extends BaseController
{
    @Autowired
    private ITfConfigStrategyBottomBreakoutService tfConfigStrategyBottomBreakoutService;

    /**
     * 查询底部反转策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        startPage();
        List<TfConfigStrategyBottomBreakout> list = tfConfigStrategyBottomBreakoutService.selectTfConfigStrategyBottomBreakoutList(tfConfigStrategyBottomBreakout);
        return getDataTable(list);
    }

    /**
     * 导出底部反转策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:export')")
    @Log(title = "底部反转策略回测参数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        List<TfConfigStrategyBottomBreakout> list = tfConfigStrategyBottomBreakoutService.selectTfConfigStrategyBottomBreakoutList(tfConfigStrategyBottomBreakout);
        ExcelUtil<TfConfigStrategyBottomBreakout> util = new ExcelUtil<TfConfigStrategyBottomBreakout>(TfConfigStrategyBottomBreakout.class);
        util.exportExcel(response, list, "底部反转策略回测参数配置数据");
    }

    /**
     * 获取底部反转策略回测参数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfConfigStrategyBottomBreakoutService.selectTfConfigStrategyBottomBreakoutById(id));
    }

    /**
     * 新增底部反转策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:add')")
    @Log(title = "底部反转策略回测参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        return toAjax(tfConfigStrategyBottomBreakoutService.insertTfConfigStrategyBottomBreakout(tfConfigStrategyBottomBreakout));
    }

    /**
     * 修改底部反转策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:edit')")
    @Log(title = "底部反转策略回测参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfConfigStrategyBottomBreakout tfConfigStrategyBottomBreakout)
    {
        return toAjax(tfConfigStrategyBottomBreakoutService.updateTfConfigStrategyBottomBreakout(tfConfigStrategyBottomBreakout));
    }

    /**
     * 删除底部反转策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:bottombreakoutconfig:remove')")
    @Log(title = "底部反转策略回测参数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfConfigStrategyBottomBreakoutService.deleteTfConfigStrategyBottomBreakoutByIds(ids));
    }
}
