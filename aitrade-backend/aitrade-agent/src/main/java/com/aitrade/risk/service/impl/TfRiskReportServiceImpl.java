package com.aitrade.risk.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.risk.mapper.TfRiskReportMapper;
import com.aitrade.risk.domain.TfRiskReport;
import com.aitrade.risk.service.ITfRiskReportService;

/**
 * AI研报Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Service
public class TfRiskReportServiceImpl implements ITfRiskReportService 
{
    @Autowired
    private TfRiskReportMapper tfRiskReportMapper;

    /**
     * 查询AI研报
     * 
     * @param id AI研报主键
     * @return AI研报
     */
    @Override
    public TfRiskReport selectTfRiskReportById(Long id)
    {
        return tfRiskReportMapper.selectTfRiskReportById(id);
    }

    /**
     * 查询AI研报列表
     * 
     * @param tfRiskReport AI研报
     * @return AI研报
     */
    @Override
    public List<TfRiskReport> selectTfRiskReportList(TfRiskReport tfRiskReport)
    {
        return tfRiskReportMapper.selectTfRiskReportList(tfRiskReport);
    }

    /**
     * 新增AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    @Override
    public int insertTfRiskReport(TfRiskReport tfRiskReport)
    {
        tfRiskReport.setCreateTime(DateUtils.getNowDate());
        return tfRiskReportMapper.insertTfRiskReport(tfRiskReport);
    }

    /**
     * 修改AI研报
     * 
     * @param tfRiskReport AI研报
     * @return 结果
     */
    @Override
    public int updateTfRiskReport(TfRiskReport tfRiskReport)
    {
        return tfRiskReportMapper.updateTfRiskReport(tfRiskReport);
    }

    /**
     * 批量删除AI研报
     * 
     * @param ids 需要删除的AI研报主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportByIds(Long[] ids)
    {
        return tfRiskReportMapper.deleteTfRiskReportByIds(ids);
    }

    /**
     * 删除AI研报信息
     * 
     * @param id AI研报主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportById(Long id)
    {
        return tfRiskReportMapper.deleteTfRiskReportById(id);
    }
}
