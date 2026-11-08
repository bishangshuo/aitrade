package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfAccountDaily;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回测账户快照Mapper接口
 * 
 * @author aitrade
 * @date 2026-11-08
 */
@Mapper
public interface TfAccountDailyMapper 
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
     * 删除回测账户快照
     * 
     * @param id 回测账户快照主键
     * @return 结果
     */
    public int deleteTfAccountDailyById(Long id);

    /**
     * 批量删除回测账户快照
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfAccountDailyByIds(Long[] ids);
}
