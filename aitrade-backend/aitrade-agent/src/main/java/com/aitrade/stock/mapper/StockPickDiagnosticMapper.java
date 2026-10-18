package com.aitrade.stock.mapper;

import com.aitrade.stock.domain.StockPickDiagnostic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StockPickDiagnosticMapper {
    /**
     * 批量保存回測與篩選明細數據
     */
    int insertBatch(@Param("list") List<StockPickDiagnostic> list);

    List<StockPickDiagnostic> selectTopScore(@Param("et") StockPickDiagnostic et, @Param("limit") int limit);
}
