package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import com.aitrade.stock.domain.*;
import com.aitrade.stock.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.service.ITfBacktestRecordService;

/**
 * 回测记录Service业务层处理
 * 
 * @author aitrade
 * @date 2026-11-05
 */
@Service
public class TfBacktestRecordServiceImpl implements ITfBacktestRecordService 
{
    @Autowired
    private TfBacktestRecordMapper tfBacktestRecordMapper;

    @Resource
    private TfConfigStrategyStrongBreakoutMapper tfConfigStrategyStrongBreakoutMapper;

    @Resource
    private TfConfigStrategyBottomBreakoutMapper tfConfigStrategyBottomBreakoutMapper;

    @Resource
    private TfConfigStrategyPullbackLowSuckMapper tfConfigStrategyPullbackLowSuckMapper;

    @Resource
    private TfConfigStrategyOversoldReboundMapper tfConfigStrategyOversoldReboundMapper;

    @Resource
    private TfConfigStrategyTradeMapper tfConfigStrategyTradeMapper;

    /**
     * 查询回测记录
     * 
     * @param id 回测记录主键
     * @return 回测记录
     */
    @Override
    public TfBacktestRecord selectTfBacktestRecordById(Long id)
    {
        TfBacktestRecord backtestRecord = tfBacktestRecordMapper.selectTfBacktestRecordById(id);
        setConfig(backtestRecord);
        return backtestRecord;
    }

    /**
     * 查询回测记录列表
     * 
     * @param tfBacktestRecord 回测记录
     * @return 回测记录
     */
    @Override
    public List<TfBacktestRecord> selectTfBacktestRecordList(TfBacktestRecord tfBacktestRecord)
    {
        List<TfBacktestRecord> list = tfBacktestRecordMapper.selectTfBacktestRecordList(tfBacktestRecord);
        if(list == null || list.isEmpty()) {
            return null;
        }
        for (TfBacktestRecord backtestRecord : list) {
            setConfig(backtestRecord);
        }
        return list;
    }

    private void setConfig(TfBacktestRecord backtestRecord) {
        TfConfigStrategyStrongBreakout strongBreakout = tfConfigStrategyStrongBreakoutMapper.selectTfConfigStrategyStrongBreakoutById(backtestRecord.getCnfStrongBreakoutId());
        backtestRecord.setCnfStrongBreakout(strongBreakout);

        TfConfigStrategyBottomBreakout bottomBreakout = tfConfigStrategyBottomBreakoutMapper.selectTfConfigStrategyBottomBreakoutById(backtestRecord.getCnfBottomBreakoutId());

        backtestRecord.setCnfBottomBreakout(bottomBreakout);

        TfConfigStrategyPullbackLowSuck pullbackLowSuck = tfConfigStrategyPullbackLowSuckMapper.selectTfConfigStrategyPullbackLowSuckById(backtestRecord.getCnfPullbackLowSuckId());

        backtestRecord.setCnfPullbackLowSuck(pullbackLowSuck);

        TfConfigStrategyOversoldRebound oversoldRebound = tfConfigStrategyOversoldReboundMapper.selectTfConfigStrategyOversoldReboundById(backtestRecord.getCnfOversoldReboundId());

        backtestRecord.setCnfOversoldRebound(oversoldRebound);
    }

    /**
     * 新增回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    @Override
    public int insertTfBacktestRecord(TfBacktestRecord tfBacktestRecord)
    {
        tfBacktestRecord.setCreateTime(DateUtils.getNowDate());

        //创建配置参数
        TfConfigStrategyStrongBreakout strongBreakout = new TfConfigStrategyStrongBreakout();


        return tfBacktestRecordMapper.insertTfBacktestRecord(tfBacktestRecord);
    }

    /**
     * 修改回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    @Override
    public int updateTfBacktestRecord(TfBacktestRecord tfBacktestRecord)
    {
        tfBacktestRecord.setUpdateTime(DateUtils.getNowDate());
        return tfBacktestRecordMapper.updateTfBacktestRecord(tfBacktestRecord);
    }

    /**
     * 批量删除回测记录
     * 
     * @param ids 需要删除的回测记录主键
     * @return 结果
     */
    @Override
    public int deleteTfBacktestRecordByIds(Long[] ids)
    {
        return tfBacktestRecordMapper.deleteTfBacktestRecordByIds(ids);
    }

    /**
     * 删除回测记录信息
     * 
     * @param id 回测记录主键
     * @return 结果
     */
    @Override
    public int deleteTfBacktestRecordById(Long id)
    {
        return tfBacktestRecordMapper.deleteTfBacktestRecordById(id);
    }
}
