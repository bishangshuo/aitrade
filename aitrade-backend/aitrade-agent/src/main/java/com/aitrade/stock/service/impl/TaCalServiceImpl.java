package com.aitrade.stock.service.impl;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.aitrade.stock.domain.vo.HolidayGap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.stock.mapper.TaCalMapper;
import com.aitrade.stock.domain.TaCal;
import com.aitrade.stock.service.ITaCalService;

/**
 * 交易日历Service业务层处理
 * 
 * @author bishangshuo
 * @date 2026-10-19
 */
@Service
public class TaCalServiceImpl implements ITaCalService 
{
    @Autowired
    private TaCalMapper taCalMapper;

    /**
     * 查询交易日历
     * 
     * @param id 交易日历主键
     * @return 交易日历
     */
    @Override
    public TaCal selectTaCalById(Long id)
    {
        return taCalMapper.selectTaCalById(id);
    }

    /**
     * 查询交易日历列表
     * 
     * @param taCal 交易日历
     * @return 交易日历
     */
    @Override
    public List<TaCal> selectTaCalList(TaCal taCal)
    {
        return taCalMapper.selectTaCalList(taCal);
    }

    /**
     * 新增交易日历
     * 
     * @param taCal 交易日历
     * @return 结果
     */
    @Override
    public int insertTaCal(TaCal taCal)
    {
        return taCalMapper.insertTaCal(taCal);
    }

    /**
     * 修改交易日历
     * 
     * @param taCal 交易日历
     * @return 结果
     */
    @Override
    public int updateTaCal(TaCal taCal)
    {
        return taCalMapper.updateTaCal(taCal);
    }

    /**
     * 批量删除交易日历
     * 
     * @param ids 需要删除的交易日历主键
     * @return 结果
     */
    @Override
    public int deleteTaCalByIds(Long[] ids)
    {
        return taCalMapper.deleteTaCalByIds(ids);
    }

    /**
     * 删除交易日历信息
     * 
     * @param id 交易日历主键
     * @return 结果
     */
    @Override
    public int deleteTaCalById(Long id)
    {
        return taCalMapper.deleteTaCalById(id);
    }

    @Override
    public HolidayGap isLongHolidayAhead(Date date) {
        // 從資料庫查詢交易日曆，時間範圍為 date+1 到 date+30 天
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date beginDate = cal.getTime();

        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date endDate = cal.getTime();

        TaCal query = new TaCal();
        query.setBeginDate(beginDate);
        query.setEndDate(endDate);
        List<TaCal> listDate = taCalMapper.selectTaCalList(query);

        // 如果資料不足，為保險起見可視為無法判斷或預設安全防禦
        if(listDate == null || listDate.isEmpty() || listDate.size() < 5) {
            HolidayGap holidayGap = new HolidayGap();
            holidayGap.setWillBeHoliday(true);
            return holidayGap;
        }

        // 嚴格限制：只檢查「5天內」（即索引 0 到 4，對應距離 1~5 天）
        // 確保陣列長度足夠做 3 天連續檢查
        int maxCheckIndex = Math.min(5, listDate.size() - 2);

        for(int i = 0; i < maxCheckIndex; i++) {
            // 檢查從 i 開始是否連續 3 天休市 (isOpen == 0)
            if(listDate.get(i).getIsOpen().equals(0)
                    && listDate.get(i+1).getIsOpen().equals(0)
                    && listDate.get(i+2).getIsOpen().equals(0)) {

                HolidayGap holidayGap = new HolidayGap();
                holidayGap.setWillBeHoliday(true);
                holidayGap.setNowDate(date);
                holidayGap.setBeginDateOfHoliday(listDate.get(i).getCalDate());
                holidayGap.setComingDistance(i + 1); // 距離天數（1 代表明天）

                // 計算真實假期長度（從 i+3 開始向後繼續檢查連續休市天數）
                Integer holidayLong = 3;
                int j = i + 3;
                while(j < listDate.size() && listDate.get(j).getIsOpen().equals(0)) {
                    holidayLong++;
                    j++;
                }
                holidayGap.setHolidayLong(holidayLong);

                // 精準尋找假期後的「第一個真實交易日」（isOpen == 1）
                Date tradeDateAfterHoliday = null;
                for(int k = i + 3; k < listDate.size(); k++) {
                    if(listDate.get(k).getIsOpen().equals(1)) {
                        tradeDateAfterHoliday = listDate.get(k).getCalDate();
                        break;
                    }
                }
                holidayGap.setTradeDateAfterHoliday(tradeDateAfterHoliday);

                return holidayGap;
            }
        }

        return null;
    }
}
