package com.aitrade.risk.mapper;

import java.util.List;
import com.aitrade.risk.domain.TfRiskReportAnn;

/**
 * 研报公告Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-27
 */
public interface TfRiskReportAnnMapper 
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
     * 删除研报公告
     * 
     * @param id 研报公告主键
     * @return 结果
     */
    public int deleteTfRiskReportAnnById(Long id);

    /**
     * 批量删除研报公告
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfRiskReportAnnByIds(Long[] ids);
}
