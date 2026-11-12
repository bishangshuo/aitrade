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
import com.aitrade.stock.domain.TfPositionDaily;
import com.aitrade.stock.service.ITfPositionDailyService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回测持仓日历Controller
 * 
 * @author aitrade
 * @date 2026-11-12
 */
@RestController
@RequestMapping("/stock/tfpositiondaily")
public class TfPositionDailyController extends BaseController
{
    @Autowired
    private ITfPositionDailyService tfPositionDailyService;

    /**
     * 查询回测持仓日历列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfPositionDaily tfPositionDaily)
    {
        startPage();
        List<TfPositionDaily> list = tfPositionDailyService.selectTfPositionDailyList(tfPositionDaily);
        return getDataTable(list);
    }

    /**
     * 导出回测持仓日历列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:export')")
    @Log(title = "回测持仓日历", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfPositionDaily tfPositionDaily)
    {
        List<TfPositionDaily> list = tfPositionDailyService.selectTfPositionDailyList(tfPositionDaily);
        ExcelUtil<TfPositionDaily> util = new ExcelUtil<TfPositionDaily>(TfPositionDaily.class);
        util.exportExcel(response, list, "回测持仓日历数据");
    }

    /**
     * 获取回测持仓日历详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfPositionDailyService.selectTfPositionDailyById(id));
    }

    /**
     * 新增回测持仓日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:add')")
    @Log(title = "回测持仓日历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfPositionDaily tfPositionDaily)
    {
        return toAjax(tfPositionDailyService.insertTfPositionDaily(tfPositionDaily));
    }

    /**
     * 修改回测持仓日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:edit')")
    @Log(title = "回测持仓日历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfPositionDaily tfPositionDaily)
    {
        return toAjax(tfPositionDailyService.updateTfPositionDaily(tfPositionDaily));
    }

    /**
     * 删除回测持仓日历
     */
    @PreAuthorize("@ss.hasPermi('stock:tfpositiondaily:remove')")
    @Log(title = "回测持仓日历", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfPositionDailyService.deleteTfPositionDailyByIds(ids));
    }
}
