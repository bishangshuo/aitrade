package com.aitrade.exchange.mapper;

import com.aitrade.exchange.domain.TradeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeRecordMapper {

    int insert(TradeRecord record);

    List<TradeRecord> findBySymbol(@Param("symbol") String symbol);

    List<TradeRecord> findOpenPosition(@Param("symbol") String symbol); // 未平仓

    TradeRecord findLastEntry(@Param("symbol") String symbol);
}
