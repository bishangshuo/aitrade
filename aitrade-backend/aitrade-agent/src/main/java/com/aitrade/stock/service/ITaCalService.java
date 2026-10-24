package com.aitrade.stock.service;

import java.util.Date;
import java.util.List;
import com.aitrade.stock.domain.TaCal;
import com.aitrade.stock.domain.vo.HolidayGap;

/**
 * 交易日历Service接口
 * 
 * @author bishangshuo
 * @date 2026-10-19
 */
public interface ITaCalService 
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
     * 批量删除交易日历
     * 
     * @param ids 需要删除的交易日历主键集合
     * @return 结果
     */
    public int deleteTaCalByIds(Long[] ids);

    /**
     * 删除交易日历信息
     * 
     * @param id 交易日历主键
     * @return 结果
     */
    public int deleteTaCalById(Long id);

    /**
     * 判断某一日历天是否之后的5天后是否为中长假
     * 中长假为国家法定节假日，有元旦、春节、清明节、五一劳动节、端午节、中秋节、国庆节。
     * 值得注意的是，A股市场中长假连休后周末调整上班日，也是休市的。
     * 但不论假期多复杂，只要5天后遇上3天休市，则认为是中长假。此时避免量化开单
     */

    public HolidayGap isLongHolidayAhead(Date date);
}
