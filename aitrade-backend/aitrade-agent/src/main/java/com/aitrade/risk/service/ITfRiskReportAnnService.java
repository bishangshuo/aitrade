package com.aitrade.risk.service;

import java.util.List;
import com.aitrade.risk.domain.TfRiskReportAnn;

/**
 * 研报公告Service接口
 * 
 * @author aitrade
 * @date 2026-10-27
 */
public interface ITfRiskReportAnnService 
{
    /**
     * 查询研报公告
     * 
     * @param id 研报公告主键
     * @return 研报公告
     */
    public TfRiskReportAnn selectTfRiskReportAnnById(Long id);

    /**
     * 查询研报公告列表
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 研报公告集合
     */
    public List<TfRiskReportAnn> selectTfRiskReportAnnList(TfRiskReportAnn tfRiskReportAnn);

    /**
     * 新增研报公告
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 结果
     */
    public int insertTfRiskReportAnn(TfRiskReportAnn tfRiskReportAnn);

    /**
     * 修改研报公告
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 结果
     */
    public int updateTfRiskReportAnn(TfRiskReportAnn tfRiskReportAnn);

    /**
     * 批量删除研报公告
     * 
     * @param ids 需要删除的研报公告主键集合
     * @return 结果
     */
    public int deleteTfRiskReportAnnByIds(Long[] ids);

    /**
     * 删除研报公告信息
     * 
     * @param id 研报公告主键
     * @return 结果
     */
    public int deleteTfRiskReportAnnById(Long id);
}
