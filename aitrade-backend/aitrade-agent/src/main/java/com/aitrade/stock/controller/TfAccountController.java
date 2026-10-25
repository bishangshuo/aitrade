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
import com.aitrade.stock.domain.TfAccount;
import com.aitrade.stock.service.ITfAccountService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 回測虛擬賬戶Controller
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@RestController
@RequestMapping("/tfstock/tfaccount")
public class TfAccountController extends BaseController
{
    @Autowired
    private ITfAccountService tfAccountService;

    /**
     * 查询回測虛擬賬戶列表
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:list')")
    @GetMapping("/list")
    public TableDataInfo list(TfAccount tfAccount)
    {
        startPage();
        List<TfAccount> list = tfAccountService.selectTfAccountList(tfAccount);
        return getDataTable(list);
    }

    /**
     * 导出回測虛擬賬戶列表
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:export')")
    @Log(title = "回測虛擬賬戶", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TfAccount tfAccount)
    {
        List<TfAccount> list = tfAccountService.selectTfAccountList(tfAccount);
        ExcelUtil<TfAccount> util = new ExcelUtil<TfAccount>(TfAccount.class);
        util.exportExcel(response, list, "回測虛擬賬戶数据");
    }

    /**
     * 获取回測虛擬賬戶详细信息
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tfAccountService.selectTfAccountById(id));
    }

    /**
     * 新增回測虛擬賬戶
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:add')")
    @Log(title = "回測虛擬賬戶", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TfAccount tfAccount)
    {
        return toAjax(tfAccountService.insertTfAccount(tfAccount));
    }

    /**
     * 修改回測虛擬賬戶
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:edit')")
    @Log(title = "回測虛擬賬戶", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TfAccount tfAccount)
    {
        return toAjax(tfAccountService.updateTfAccount(tfAccount));
    }

    /**
     * 删除回測虛擬賬戶
     */
    @PreAuthorize("@ss.hasPermi('tfstock:tfaccount:remove')")
    @Log(title = "回測虛擬賬戶", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tfAccountService.deleteTfAccountByIds(ids));
    }
}
