package com.aitrade.risk.service;

import java.util.Date;
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
     * 从东方财富网获取某个股票从某个日期开始往前、30天内count条风险公告
      * @param stockCode 股票代码
      * @param fromDate 开始日期，
     *                 传入的是T交易日当天的最后时间，如：2026-06-09 23:59:59,
     *                 但是，在回测中，传入的是T+1交易日的盘前时间，如本例中的2026-06-10 09:15:00,这样就能在回测中真实过滤隔夜黑天鹤
      * @param period 获取天数区间
      * @param count 获取条数
     *
     */
    public List<TfRiskAnn> getRiskAnnFromEastMoneyBackward(String stockCode, Date fromDate, int period, int count);
}
