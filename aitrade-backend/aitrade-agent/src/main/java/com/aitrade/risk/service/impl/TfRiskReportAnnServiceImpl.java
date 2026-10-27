package com.aitrade.risk.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.risk.mapper.TfRiskReportAnnMapper;
import com.aitrade.risk.domain.TfRiskReportAnn;
import com.aitrade.risk.service.ITfRiskReportAnnService;

/**
 * 研报公告Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Service
public class TfRiskReportAnnServiceImpl implements ITfRiskReportAnnService 
{
    @Autowired
    private TfRiskReportAnnMapper tfRiskReportAnnMapper;

    /**
     * 查询研报公告
     * 
     * @param id 研报公告主键
     * @return 研报公告
     */
    @Override
    public TfRiskReportAnn selectTfRiskReportAnnById(Long id)
    {
        return tfRiskReportAnnMapper.selectTfRiskReportAnnById(id);
    }

    /**
     * 查询研报公告列表
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 研报公告
     */
    @Override
    public List<TfRiskReportAnn> selectTfRiskReportAnnList(TfRiskReportAnn tfRiskReportAnn)
    {
        return tfRiskReportAnnMapper.selectTfRiskReportAnnList(tfRiskReportAnn);
    }

    /**
     * 新增研报公告
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 结果
     */
    @Override
    public int insertTfRiskReportAnn(TfRiskReportAnn tfRiskReportAnn)
    {
        tfRiskReportAnn.setCreateTime(DateUtils.getNowDate());
        return tfRiskReportAnnMapper.insertTfRiskReportAnn(tfRiskReportAnn);
    }

    /**
     * 修改研报公告
     * 
     * @param tfRiskReportAnn 研报公告
     * @return 结果
     */
    @Override
    public int updateTfRiskReportAnn(TfRiskReportAnn tfRiskReportAnn)
    {
        return tfRiskReportAnnMapper.updateTfRiskReportAnn(tfRiskReportAnn);
    }

    /**
     * 批量删除研报公告
     * 
     * @param ids 需要删除的研报公告主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportAnnByIds(Long[] ids)
    {
        return tfRiskReportAnnMapper.deleteTfRiskReportAnnByIds(ids);
    }

    /**
     * 删除研报公告信息
     * 
     * @param id 研报公告主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskReportAnnById(Long id)
    {
        return tfRiskReportAnnMapper.deleteTfRiskReportAnnById(id);
    }
}
