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
import com.aitrade.stock.domain.TfConfigStrategyPullbackLowSuck;
import com.aitrade.stock.service.ITfConfigStrategyPullbackLowSuckService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 震荡攀升低吸策略回测参数配置Controller
 * 
 * @author aitrade
 * @date 2026-11-03
 */
@RestController
@RequestMapping("/stock/pullbacklowsuckconfig")
public class TfConfigStrategyPullbackLowSuckController extends BaseController
{
    @Autowired
    private ITfConfigStrategyPullbackLowSuckService tfConfigStrategyPullbackLowSuckService;

    /**
     * 查询震荡攀升低吸策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        startPage();
        List<TfConfigStrategyPullbackLowSuck> list = tfConfigStrategyPullbackLowSuckService.selectTfConfigStrategyPullbackLowSuckList(tfConfigStrategyPullbackLowSuck);
        return getDataTable(list);
    }

    /**
     * 导出震荡攀升低吸策略回测参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:export')")
    @Log(title = "震荡攀升低吸策略回测参数配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        List<TfConfigStrategyPullbackLowSuck> list = tfConfigStrategyPullbackLowSuckService.selectTfConfigStrategyPullbackLowSuckList(tfConfigStrategyPullbackLowSuck);
        ExcelUtil<TfConfigStrategyPullbackLowSuck> util = new ExcelUtil<TfConfigStrategyPullbackLowSuck>(TfConfigStrategyPullbackLowSuck.class);
        util.exportExcel(response, list, "震荡攀升低吸策略回测参数配置数据");
    }

    /**
     * 获取震荡攀升低吸策略回测参数配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfConfigStrategyPullbackLowSuckService.selectTfConfigStrategyPullbackLowSuckById(id));
    }

    /**
     * 新增震荡攀升低吸策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:add')")
    @Log(title = "震荡攀升低吸策略回测参数配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        return toAjax(tfConfigStrategyPullbackLowSuckService.insertTfConfigStrategyPullbackLowSuck(tfConfigStrategyPullbackLowSuck));
    }

    /**
     * 修改震荡攀升低吸策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:edit')")
    @Log(title = "震荡攀升低吸策略回测参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfConfigStrategyPullbackLowSuck tfConfigStrategyPullbackLowSuck)
    {
        return toAjax(tfConfigStrategyPullbackLowSuckService.updateTfConfigStrategyPullbackLowSuck(tfConfigStrategyPullbackLowSuck));
    }

    /**
     * 删除震荡攀升低吸策略回测参数配置
     */
    @PreAuthorize("@ss.hasPermi('stock:pullbacklowsuckconfig:remove')")
    @Log(title = "震荡攀升低吸策略回测参数配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfConfigStrategyPullbackLowSuckService.deleteTfConfigStrategyPullbackLowSuckByIds(ids));
    }
}
