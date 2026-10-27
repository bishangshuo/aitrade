package com.aitrade.risk.mapper;

import java.util.List;
import com.aitrade.risk.domain.TfRiskAnn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 风险公告Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Mapper
public interface TfRiskAnnMapper 
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
     * 删除风险公告
     * 
     * @param id 风险公告主键
     * @return 结果
     */
    public int deleteTfRiskAnnById(Long id);

    /**
     * 批量删除风险公告
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfRiskAnnByIds(Long[] ids);
}
