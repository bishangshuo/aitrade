package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfAccountDailyMapper;
import com.aitrade.stock.domain.TfAccountDaily;
import com.aitrade.stock.service.ITfAccountDailyService;

/**
 * 回测账户快照Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-08
 */
@Service
public class TfAccountDailyServiceImpl implements ITfAccountDailyService 
{
    @Autowired
    private TfAccountDailyMapper tfAccountDailyMapper;

    /**
     * 查询回测账户快照
     * 
     * @param id 回测账户快照主键
     * @return 回测账户快照
     */
    @Override
    public TfAccountDaily selectTfAccountDailyById(Long id)
    {
        return tfAccountDailyMapper.selectTfAccountDailyById(id);
    }

    /**
     * 查询回测账户快照列表
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 回测账户快照
     */
    @Override
    public List<TfAccountDaily> selectTfAccountDailyList(TfAccountDaily tfAccountDaily)
    {
        return tfAccountDailyMapper.selectTfAccountDailyList(tfAccountDaily);
    }

    /**
     * 新增回测账户快照
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 结果
     */
    @Override
    public int insertTfAccountDaily(TfAccountDaily tfAccountDaily)
    {
        tfAccountDaily.setCreateTime(DateUtils.getNowDate());
        return tfAccountDailyMapper.insertTfAccountDaily(tfAccountDaily);
    }

    /**
     * 修改回测账户快照
     * 
     * @param tfAccountDaily 回测账户快照
     * @return 结果
     */
    @Override
    public int updateTfAccountDaily(TfAccountDaily tfAccountDaily)
    {
        tfAccountDaily.setUpdateTime(DateUtils.getNowDate());
        return tfAccountDailyMapper.updateTfAccountDaily(tfAccountDaily);
    }

    /**
     * 批量删除回测账户快照
     * 
     * @param ids 需要删除的回测账户快照主键
     * @return 结果
     */
    @Override
    public int deleteTfAccountDailyByIds(Long[] ids)
    {
        return tfAccountDailyMapper.deleteTfAccountDailyByIds(ids);
    }

    /**
     * 删除回测账户快照信息
     * 
     * @param id 回测账户快照主键
     * @return 结果
     */
    @Override
    public int deleteTfAccountDailyById(Long id)
    {
        return tfAccountDailyMapper.deleteTfAccountDailyById(id);
    }
}
