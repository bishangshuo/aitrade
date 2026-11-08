package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import com.aitrade.stock.domain.*;
import com.aitrade.stock.mapper.*;
import com.aitrade.stock.strategy.enums.StrategyType;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.service.ITfBacktestRecordService;
import org.springframework.transaction.annotation.Transactional;

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

        TfConfigStrategyTrade tradeConfig = tfConfigStrategyTradeMapper.selectTfConfigStrategyTradeById(backtestRecord.getCnfTradeId());
        backtestRecord.setCnfTrade(tradeConfig);

    }

    /**
     * 新增回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTfBacktestRecord(TfBacktestRecord tfBacktestRecord)
    {
        tfBacktestRecord.setCreateTime(DateUtils.getNowDate());

        String backTestCode = tfBacktestRecord.getCode();
        String backTestName = tfBacktestRecord.getName();

        //创建配置参数
        //创建默认主升浪突破策略参数配置
        TfConfigStrategyStrongBreakout strongBreakout = new TfConfigStrategyStrongBreakout();
        strongBreakout.setConfigName("cnf_strong_breakout_" + backTestCode);
        strongBreakout.setStrategyCode(StrategyType.STRONG_BREAKOUT.getCode());
        strongBreakout.setStrategyName(StrategyType.STRONG_BREAKOUT.getDescription());
        tfConfigStrategyStrongBreakoutMapper.insertTfConfigStrategyStrongBreakout(strongBreakout);
        tfBacktestRecord.setCnfStrongBreakoutId(strongBreakout.getId());

        //创建底部反转策略参数配置
        TfConfigStrategyBottomBreakout bottomBreakout = new TfConfigStrategyBottomBreakout();
        bottomBreakout.setConfigName("cnf_bottom_breakout_" + backTestCode);
        bottomBreakout.setStrategyCode(StrategyType.BOTTOM_BREAKOUT.getCode());
        bottomBreakout.setStrategyName(StrategyType.BOTTOM_BREAKOUT.getDescription());
        tfConfigStrategyBottomBreakoutMapper.insertTfConfigStrategyBottomBreakout(bottomBreakout);
        tfBacktestRecord.setCnfBottomBreakoutId(bottomBreakout.getId());

        //创建超跌反弹策略参数配置
        TfConfigStrategyOversoldRebound oversoldRebound = new TfConfigStrategyOversoldRebound();
        oversoldRebound.setConfigName("cnf_oversold_rebound_" + backTestCode);
        oversoldRebound.setStrategyCode(StrategyType.OVERSOLD_REBOUND.getCode());
        oversoldRebound.setStrategyName(StrategyType.OVERSOLD_REBOUND.getDescription());
        tfConfigStrategyOversoldReboundMapper.insertTfConfigStrategyOversoldRebound(oversoldRebound);
        tfBacktestRecord.setCnfOversoldReboundId(oversoldRebound.getId());

        //创建震荡攀升低吸策略参数配置
        TfConfigStrategyPullbackLowSuck pullbackLowSuck = new TfConfigStrategyPullbackLowSuck();
        pullbackLowSuck.setConfigName("cnf_pullback_low_suck_" + backTestCode);
        pullbackLowSuck.setStrategyCode(StrategyType.PULLBACK_LOW_SUCK.getCode());
        pullbackLowSuck.setStrategyName(StrategyType.PULLBACK_LOW_SUCK.getDescription());
        tfConfigStrategyPullbackLowSuckMapper.insertTfConfigStrategyPullbackLowSuck(pullbackLowSuck);
        tfBacktestRecord.setCnfPullbackLowSuckId(pullbackLowSuck.getId());

        //创建交易策略参数配置
        TfConfigStrategyTrade trade = new TfConfigStrategyTrade();
        trade.setConfigName("cnf_trade_" + backTestCode);
        tfConfigStrategyTradeMapper.insertTfConfigStrategyTrade(trade);
        tfBacktestRecord.setCnfTradeId(trade.getId());

        return tfBacktestRecordMapper.insertTfBacktestRecord(tfBacktestRecord);
    }

    /**
     * 修改回测记录
     * 
     * @param tfBacktestRecord 回测记录
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTfBacktestRecord(TfBacktestRecord tfBacktestRecord)
    {
        tfBacktestRecord.setUpdateTime(DateUtils.getNowDate());
        tfConfigStrategyStrongBreakoutMapper.updateTfConfigStrategyStrongBreakout(tfBacktestRecord.getCnfStrongBreakout());
        tfConfigStrategyBottomBreakoutMapper.updateTfConfigStrategyBottomBreakout(tfBacktestRecord.getCnfBottomBreakout());
        tfConfigStrategyPullbackLowSuckMapper.updateTfConfigStrategyPullbackLowSuck(tfBacktestRecord.getCnfPullbackLowSuck());
        tfConfigStrategyOversoldReboundMapper.updateTfConfigStrategyOversoldRebound(tfBacktestRecord.getCnfOversoldRebound());
        tfConfigStrategyTradeMapper.updateTfConfigStrategyTrade(tfBacktestRecord.getCnfTrade());
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
        for(Long id : ids) {
            deleteTfBacktestRecordById(id);
        }
        return ids.length;
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
        TfBacktestRecord tfBacktestRecord = tfBacktestRecordMapper.selectTfBacktestRecordById(id);
        tfConfigStrategyStrongBreakoutMapper.deleteTfConfigStrategyStrongBreakoutById(tfBacktestRecord.getCnfStrongBreakoutId());
        tfConfigStrategyBottomBreakoutMapper.deleteTfConfigStrategyBottomBreakoutById(tfBacktestRecord.getCnfBottomBreakoutId());
        tfConfigStrategyPullbackLowSuckMapper.deleteTfConfigStrategyPullbackLowSuckById(tfBacktestRecord.getCnfPullbackLowSuckId());
        tfConfigStrategyOversoldReboundMapper.deleteTfConfigStrategyOversoldReboundById(tfBacktestRecord.getCnfOversoldReboundId());
        tfConfigStrategyTradeMapper.deleteTfConfigStrategyTradeById(tfBacktestRecord.getCnfTradeId());
        return tfBacktestRecordMapper.deleteTfBacktestRecordById(id);
    }
}
