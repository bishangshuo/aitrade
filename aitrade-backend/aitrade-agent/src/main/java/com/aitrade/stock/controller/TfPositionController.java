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
import com.aitrade.stock.domain.TfPosition;
import com.aitrade.stock.service.ITfPositionService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回測當前持倉Controller
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@RestController
@RequestMapping("/stock/tfposition")
public class TfPositionController extends BaseController
{
    @Autowired
    private ITfPositionService tfPositionService;

    /**
     * 查询回測當前持倉列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfPosition tfPosition)
    {
        startPage();
        List<TfPosition> list = tfPositionService.selectTfPositionList(tfPosition);
        return getDataTable(list);
    }

    /**
     * 导出回測當前持倉列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:export')")
    @Log(title = "回測當前持倉", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfPosition tfPosition)
    {
        List<TfPosition> list = tfPositionService.selectTfPositionList(tfPosition);
        ExcelUtil<TfPosition> util = new ExcelUtil<TfPosition>(TfPosition.class);
        util.exportExcel(response, list, "回測當前持倉数据");
    }

    /**
     * 获取回測當前持倉详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfPositionService.selectTfPositionById(id));
    }

    /**
     * 新增回測當前持倉
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:add')")
    @Log(title = "回測當前持倉", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfPosition tfPosition)
    {
        return toAjax(tfPositionService.insertTfPosition(tfPosition));
    }

    /**
     * 修改回測當前持倉
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:edit')")
    @Log(title = "回測當前持倉", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfPosition tfPosition)
    {
        return toAjax(tfPositionService.updateTfPosition(tfPosition));
    }

    /**
     * 删除回測當前持倉
     */
    @PreAuthorize("@ss.hasPermi('stock:tfposition:remove')")
    @Log(title = "回測當前持倉", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfPositionService.deleteTfPositionByIds(ids));
    }
}
