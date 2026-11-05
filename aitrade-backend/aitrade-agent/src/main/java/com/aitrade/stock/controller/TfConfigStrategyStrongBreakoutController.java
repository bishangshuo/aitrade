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
import com.aitrade.stock.domain.TfConfigStrategyStrongBreakout;
import com.aitrade.stock.service.ITfConfigStrategyStrongBreakoutService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 主升浪策略回测参数配置Controller
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/stock/strongbreakoutconfig")
public class TfConfigStrategyStrongBreakoutController extends BaseController
{
    @Autowired
    private ITfConfigStrategyStrongBreakoutService tfConfigStrategyStrongBreakoutService;

    /**
     * 查询主升浪策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        startPage();
        List<TfConfigStrategyStrongBreakout> list = tfConfigStrategyStrongBreakoutService.selectTfConfigStrategyStrongBreakoutList(tfConfigStrategyStrongBreakout);
        return getDataTable(list);
    }

    /**
     * 导出主升浪策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:export')")
    @Log(title = "主升浪策略回测参数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        List<TfConfigStrategyStrongBreakout> list = tfConfigStrategyStrongBreakoutService.selectTfConfigStrategyStrongBreakoutList(tfConfigStrategyStrongBreakout);
        ExcelUtil<TfConfigStrategyStrongBreakout> util = new ExcelUtil<TfConfigStrategyStrongBreakout>(TfConfigStrategyStrongBreakout.class);
        util.exportExcel(response, list, "主升浪策略回测参数配置数据");
    }

    /**
     * 获取主升浪策略回测参数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfConfigStrategyStrongBreakoutService.selectTfConfigStrategyStrongBreakoutById(id));
    }

    /**
     * 新增主升浪策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:add')")
    @Log(title = "主升浪策略回测参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        return toAjax(tfConfigStrategyStrongBreakoutService.insertTfConfigStrategyStrongBreakout(tfConfigStrategyStrongBreakout));
    }

    /**
     * 修改主升浪策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:edit')")
    @Log(title = "主升浪策略回测参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfConfigStrategyStrongBreakout tfConfigStrategyStrongBreakout)
    {
        return toAjax(tfConfigStrategyStrongBreakoutService.updateTfConfigStrategyStrongBreakout(tfConfigStrategyStrongBreakout));
    }

    /**
     * 删除主升浪策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strongbreakoutconfig:remove')")
    @Log(title = "主升浪策略回测参数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfConfigStrategyStrongBreakoutService.deleteTfConfigStrategyStrongBreakoutByIds(ids));
    }
}
