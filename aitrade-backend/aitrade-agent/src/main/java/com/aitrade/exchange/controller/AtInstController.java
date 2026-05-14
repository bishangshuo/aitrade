package com.aitrade.exchange.controller;

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
import com.aitrade.exchange.domain.AtInst;
import com.aitrade.exchange.service.IAtInstService;
import com.aitrade.common.utils.poi.ExcelUtil;
import com.aitrade.common.core.page.TableDataInfo;

/**
 * 币种管理Controller
 * 
 * @author aitrade
 * @date 2026-05-14
 */
@RestController
@RequestMapping("/exchange/instid")
public class AtInstController extends BaseController
{
    @Autowired
    private IAtInstService atInstService;

    /**
     * 查询币种管理列表
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:list')")
    @GetMapping("/list")
    public TableDataInfo list(AtInst atInst)
    {
        startPage();
        List<AtInst> list = atInstService.selectAtInstList(atInst);
        return getDataTable(list);
    }

    /**
     * 导出币种管理列表
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:export')")
    @Log(title = "币种管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AtInst atInst)
    {
        List<AtInst> list = atInstService.selectAtInstList(atInst);
        ExcelUtil<AtInst> util = new ExcelUtil<AtInst>(AtInst.class);
        util.exportExcel(response, list, "币种管理数据");
    }

    /**
     * 获取币种管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(atInstService.selectAtInstById(id));
    }

    /**
     * 新增币种管理
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:add')")
    @Log(title = "币种管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AtInst atInst)
    {
        return toAjax(atInstService.insertAtInst(atInst));
    }

    /**
     * 修改币种管理
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:edit')")
    @Log(title = "币种管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AtInst atInst)
    {
        return toAjax(atInstService.updateAtInst(atInst));
    }

    /**
     * 删除币种管理
     */
    @PreAuthorize("@ss.hasPermi('exchange:instid:remove')")
    @Log(title = "币种管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(atInstService.deleteAtInstByIds(ids));
    }
}
