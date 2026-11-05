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
import com.aitrade.stock.domain.TfConfigStrategyOversoldRebound;
import com.aitrade.stock.service.ITfConfigStrategyOversoldReboundService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 超跌反弹策略回测参数配置Controller
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/stock/oversoldreboundconfig")
public class TfConfigStrategyOversoldReboundController extends BaseController
{
    @Autowired
    private ITfConfigStrategyOversoldReboundService tfConfigStrategyOversoldReboundService;

    /**
     * 查询超跌反弹策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        startPage();
        List<TfConfigStrategyOversoldRebound> list = tfConfigStrategyOversoldReboundService.selectTfConfigStrategyOversoldReboundList(tfConfigStrategyOversoldRebound);
        return getDataTable(list);
    }

    /**
     * 导出超跌反弹策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:export')")
    @Log(title = "超跌反弹策略回测参数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        List<TfConfigStrategyOversoldRebound> list = tfConfigStrategyOversoldReboundService.selectTfConfigStrategyOversoldReboundList(tfConfigStrategyOversoldRebound);
        ExcelUtil<TfConfigStrategyOversoldRebound> util = new ExcelUtil<TfConfigStrategyOversoldRebound>(TfConfigStrategyOversoldRebound.class);
        util.exportExcel(response, list, "超跌反弹策略回测参数配置数据");
    }

    /**
     * 获取超跌反弹策略回测参数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfConfigStrategyOversoldReboundService.selectTfConfigStrategyOversoldReboundById(id));
    }

    /**
     * 新增超跌反弹策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:add')")
    @Log(title = "超跌反弹策略回测参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        return toAjax(tfConfigStrategyOversoldReboundService.insertTfConfigStrategyOversoldRebound(tfConfigStrategyOversoldRebound));
    }

    /**
     * 修改超跌反弹策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:edit')")
    @Log(title = "超跌反弹策略回测参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfConfigStrategyOversoldRebound tfConfigStrategyOversoldRebound)
    {
        return toAjax(tfConfigStrategyOversoldReboundService.updateTfConfigStrategyOversoldRebound(tfConfigStrategyOversoldRebound));
    }

    /**
     * 删除超跌反弹策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:oversoldreboundconfig:remove')")
    @Log(title = "超跌反弹策略回测参数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfConfigStrategyOversoldReboundService.deleteTfConfigStrategyOversoldReboundByIds(ids));
    }
}
