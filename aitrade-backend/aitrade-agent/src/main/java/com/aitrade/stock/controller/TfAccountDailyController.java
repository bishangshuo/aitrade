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
import com.aitrade.stock.domain.TfAccountDaily;
import com.aitrade.stock.service.ITfAccountDailyService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回测账户快照Controller
 * 
 * @author aitrade
 * @date 2026-11-08
 */
@RestController
@RequestMapping("/stock/tfaccountdaily")
public class TfAccountDailyController extends BaseController
{
    @Autowired
    private ITfAccountDailyService tfAccountDailyService;

    /**
     * 查询回测账户快照列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfAccountDaily tfAccountDaily)
    {
        startPage();
        List<TfAccountDaily> list = tfAccountDailyService.selectTfAccountDailyList(tfAccountDaily);
        return getDataTable(list);
    }

    /**
     * 导出回测账户快照列表
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:export')")
    @Log(title = "回测账户快照", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfAccountDaily tfAccountDaily)
    {
        List<TfAccountDaily> list = tfAccountDailyService.selectTfAccountDailyList(tfAccountDaily);
        ExcelUtil<TfAccountDaily> util = new ExcelUtil<TfAccountDaily>(TfAccountDaily.class);
        util.exportExcel(response, list, "回测账户快照数据");
    }

    /**
     * 获取回测账户快照详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfAccountDailyService.selectTfAccountDailyById(id));
    }

    /**
     * 新增回测账户快照
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:add')")
    @Log(title = "回测账户快照", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfAccountDaily tfAccountDaily)
    {
        return toAjax(tfAccountDailyService.insertTfAccountDaily(tfAccountDaily));
    }

    /**
     * 修改回测账户快照
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:edit')")
    @Log(title = "回测账户快照", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfAccountDaily tfAccountDaily)
    {
        return toAjax(tfAccountDailyService.updateTfAccountDaily(tfAccountDaily));
    }

    /**
     * 删除回测账户快照
     */
    @PreAuthorize("@ss.hasPermi('stock:tfaccountdaily:remove')")
    @Log(title = "回测账户快照", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfAccountDailyService.deleteTfAccountDailyByIds(ids));
    }
}
