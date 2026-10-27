package com.aitrade.risk.service;

import java.util.List;
import com.aitrade.risk.domain.TfRiskAnn;

/**
 * 风险公告Service接口
 * 
 * @author aitrade
 * @date 2026-10-27
 */
public interface ITfRiskAnnService 
{
    /**
     * 查询风险公告
     * 
     * @param id 风险公告主键
     * @return 风险公告
     */
    public TfRiskAnn selectTfRiskAnnById(Long id);

    /**
     * 查询风险公告列表
     * 
     * @param tfRiskAnn 风险公告
     * @return 风险公告集合
     */
    public List<TfRiskAnn> selectTfRiskAnnList(TfRiskAnn tfRiskAnn);

    /**
     * 新增风险公告
     * 
     * @param tfRiskAnn 风险公告
     * @return 结果
     */
    public int insertTfRiskAnn(TfRiskAnn tfRiskAnn);

    /**
     * 修改风险公告
     * 
     * @param tfRiskAnn 风险公告
     * @return 结果
     */
    public int updateTfRiskAnn(TfRiskAnn tfRiskAnn);

    /**
     * 批量删除风险公告
     * 
     * @param ids 需要删除的风险公告主键集合
     * @return 结果
     */
    public int deleteTfRiskAnnByIds(Long[] ids);

    /**
     * 删除风险公告信息
     * 
     * @param id 风险公告主键
     * @return 结果
     */
    public int deleteTfRiskAnnById(Long id);

    /**
     * 从东方财富网获取某个股票最新的50条风险公告
     */
    public List<TfRiskAnn> getRiskAnnFromEastMoney(String stockCode);
}
