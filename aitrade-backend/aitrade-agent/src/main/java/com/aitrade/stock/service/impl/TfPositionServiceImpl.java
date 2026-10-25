package com.aitrade.stock.service.impl;

import java.util.List;
import com.aitrade.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TfPositionMapper;
import com.aitrade.stock.domain.TfPosition;
import com.aitrade.stock.service.ITfPositionService;

/**
 * 回測當前持倉Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Service
public class TfPositionServiceImpl implements ITfPositionService 
{
    @Autowired
    private TfPositionMapper tfPositionMapper;

    /**
     * 查询回測當前持倉
     * 
     * @param id 回測當前持倉主键
     * @return 回測當前持倉
     */
    @Override
    public TfPosition selectTfPositionById(Long id)
    {
        return tfPositionMapper.selectTfPositionById(id);
    }

    /**
     * 查询回測當前持倉列表
     * 
     * @param tfPosition 回測當前持倉
     * @return 回測當前持倉
     */
    @Override
    public List<TfPosition> selectTfPositionList(TfPosition tfPosition)
    {
        return tfPositionMapper.selectTfPositionList(tfPosition);
    }

    /**
     * 新增回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    @Override
    public int insertTfPosition(TfPosition tfPosition)
    {
        tfPosition.setCreateTime(DateUtils.getNowDate());
        return tfPositionMapper.insertTfPosition(tfPosition);
    }

    /**
     * 修改回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    @Override
    public int updateTfPosition(TfPosition tfPosition)
    {
        tfPosition.setUpdateTime(DateUtils.getNowDate());
        return tfPositionMapper.updateTfPosition(tfPosition);
    }

    /**
     * 批量删除回測當前持倉
     * 
     * @param ids 需要删除的回測當前持倉主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionByIds(Long[] ids)
    {
        return tfPositionMapper.deleteTfPositionByIds(ids);
    }

    /**
     * 删除回測當前持倉信息
     * 
     * @param id 回測當前持倉主键
     * @return 结果
     */
    @Override
    public int deleteTfPositionById(Long id)
    {
        return tfPositionMapper.deleteTfPositionById(id);
    }
}
