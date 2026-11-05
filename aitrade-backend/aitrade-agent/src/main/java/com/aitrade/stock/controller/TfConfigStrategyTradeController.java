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
import com.aitrade.stock.domain.TfConfigStrategyTrade;
import com.aitrade.stock.service.ITfConfigStrategyTradeService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 交易策略参数配置Controller
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/stock/strategytradeconfig")
public class TfConfigStrategyTradeController extends BaseController
{
    @Autowired
    private ITfConfigStrategyTradeService tfConfigStrategyTradeService;

    /**
     * 查询交易策略参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        startPage();
        List<TfConfigStrategyTrade> list = tfConfigStrategyTradeService.selectTfConfigStrategyTradeList(tfConfigStrategyTrade);
        return getDataTable(list);
    }

    /**
     * 导出交易策略参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:export')")
    @Log(title = "交易策略参数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        List<TfConfigStrategyTrade> list = tfConfigStrategyTradeService.selectTfConfigStrategyTradeList(tfConfigStrategyTrade);
        ExcelUtil<TfConfigStrategyTrade> util = new ExcelUtil<TfConfigStrategyTrade>(TfConfigStrategyTrade.class);
        util.exportExcel(response, list, "交易策略参数配置数据");
    }

    /**
     * 获取交易策略参数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfConfigStrategyTradeService.selectTfConfigStrategyTradeById(id));
    }

    /**
     * 新增交易策略参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:add')")
    @Log(title = "交易策略参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        return toAjax(tfConfigStrategyTradeService.insertTfConfigStrategyTrade(tfConfigStrategyTrade));
    }

    /**
     * 修改交易策略参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:edit')")
    @Log(title = "交易策略参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfConfigStrategyTrade tfConfigStrategyTrade)
    {
        return toAjax(tfConfigStrategyTradeService.updateTfConfigStrategyTrade(tfConfigStrategyTrade));
    }

    /**
     * 删除交易策略参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:strategytradeconfig:remove')")
    @Log(title = "交易策略参数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfConfigStrategyTradeService.deleteTfConfigStrategyTradeByIds(ids));
    }
}
