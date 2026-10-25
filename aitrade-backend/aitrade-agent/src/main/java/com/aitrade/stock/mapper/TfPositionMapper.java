package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TfPosition;

/**
 * 回測當前持倉Mapper接口
 * 
 * @author aitrade
 * @date 2026-10-25
 */
public interface TfPositionMapper 
{
    /**
     * 查询回測當前持倉
     * 
     * @param id 回測當前持倉主键
     * @return 回測當前持倉
     */
    public TfPosition selectTfPositionById(Long id);

    /**
     * 查询回測當前持倉列表
     * 
     * @param tfPosition 回測當前持倉
     * @return 回測當前持倉集合
     */
    public List<TfPosition> selectTfPositionList(TfPosition tfPosition);

    /**
     * 新增回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    public int insertTfPosition(TfPosition tfPosition);

    /**
     * 修改回測當前持倉
     * 
     * @param tfPosition 回測當前持倉
     * @return 结果
     */
    public int updateTfPosition(TfPosition tfPosition);

    /**
     * 删除回測當前持倉
     * 
     * @param id 回測當前持倉主键
     * @return 结果
     */
    public int deleteTfPositionById(Long id);

    /**
     * 批量删除回測當前持倉
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTfPositionByIds(Long[] ids);
}
