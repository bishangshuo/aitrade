package com.aitrade.risk.service;

import java.util.List;
import com.aitrade.risk.domain.TfRiskReport;

/**
 * AI研报Service接口
 * 
 * @author aitrade
 * @date 2026-10-27
 */
public interface ITfRiskReportService 
{
    /**
     * 查询AI研报
     * 
     * @param id AI研报主键
     * @return AI研报
     */
    public TfRiskReport selectTfRiskReportById(Long id);

    /**
     * 查询AI研报列表
     * 
     * @param tfRiskReport AI研报
     * @return AI研报集合
     */
    public List<TfRiskReport> selectTfRiskReportList(TfRiskReport tfRiskReport);

    /**
     * 新增AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    public int insertTfRiskReport(TfRiskReport tfRiskReport);

    /**
     * 修改AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    public int updateTfRiskReport(TfRiskReport tfRiskReport);

    /**
     * 批量删除AI研报
     * 
     * @param ids 需要删除的AI研报主键集合
     * @return 结果
     */
    public int deleteTfRiskReportByIds(Long[] ids);

    /**
     * 删除AI研报信息
     * 
     * @param id AI研报主键
     * @return 结果
     */
    public int deleteTfRiskReportById(Long id);
}
