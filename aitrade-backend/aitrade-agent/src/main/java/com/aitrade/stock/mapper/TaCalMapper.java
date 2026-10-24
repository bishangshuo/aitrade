package com.aitrade.stock.mapper;

import java.util.List;
import com.aitrade.stock.domain.TaCal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易日历Mapper接口
 * 
 * @author bishangshuo
 * @date 2026-10-19
 */
@Mapper
public interface TaCalMapper 
{
    /**
     * 查询交易日历
     * 
     * @param id 交易日历主键
     * @return 交易日历
     */
    public TaCal selectTaCalById(Long id);

    /**
     * 查询交易日历列表
     * 
     * @param taCal 交易日历
     * @return 交易日历集合
     */
    public List<TaCal> selectTaCalList(TaCal taCal);

    /**
     * 新增交易日历
     * 
     * @param taCal 交易日历
     * @return 结果
     */
    public int insertTaCal(TaCal taCal);

    /**
     * 修改交易日历
     * 
     * @param taCal 交易日历
     * @return 结果
     */
    public int updateTaCal(TaCal taCal);

    /**
     * 删除交易日历
     * 
     * @param id 交易日历主键
     * @return 结果
     */
    public int deleteTaCalById(Long id);

    /**
     * 批量删除交易日历
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTaCalByIds(Long[] ids);
}
