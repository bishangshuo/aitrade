package com.aitrade.stock.service;

import java.util.Date;
import java.util.List;
import com.aitrade.stock.domain.TfAccountDaily;

/**
 * 回测账户快照Service接口
 * 
 * @author aitrade
 * @date 2026-11-08
 */
public interface ITfAccountDailyService 
{
    /**
     * 查询回测账户快照
     * 
     * @param id 回测账户快照主键
     * @return 回测账户快照
     */
    public TfAccountDaily selectTfAccountDailyById(Long id);

    /**
     * 查询回测账户快照列表
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 回测账户快照集合
     */
    public List<TfAccountDaily> selectTfAccountDailyList(TfAccountDaily tfAccountDaily);

    /**
     * 新增回测账户快照
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 结果
     */
    public int insertTfAccountDaily(TfAccountDaily tfAccountDaily);

    /**
     * 修改回测账户快照
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 结果
     */
    public int updateTfAccountDaily(TfAccountDaily tfAccountDaily);

    /**
     * 批量删除回测账户快照
     * 
     * @param ids 需要删除的回测账户快照主键集合
     * @return 结果
     */
    public int deleteTfAccountDailyByIds(Long[] ids);

    /**
     * 删除回测账户快照信息
     * 
     * @param id 回测账户快照主键
     * @return 结果
     */
    public int deleteTfAccountDailyById(Long id);

    TfAccountDaily getTfAccountDailyByAccountNoAndBacktestIdAndDate(String accountNo, Long backtestId, Date date);
}
