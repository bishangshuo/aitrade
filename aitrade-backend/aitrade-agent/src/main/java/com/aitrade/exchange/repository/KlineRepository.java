package com.aitrade.exchange.repository;


import com.aitrade.exchange.domain.Kline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * K线数据仓库 - 使用TimescaleDB（基于PostgreSQL）
 *
 * 注意：使用独立配置的TimescaleDB数据源，与MySQL业务数据库分离
 */
@Repository
public class KlineRepository {

    private static final Logger log = LoggerFactory.getLogger(KlineRepository.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public KlineRepository(@Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 插入或更新K线数据
     * 使用PostgreSQL/TimescaleDB的UPSERT语法（ON CONFLICT）
     * 当遇到相同symbol+open_time的记录时，更新所有字段
     */
    public void upsert(Kline k) {
        String sql = """
            INSERT INTO kline_1m(symbol, open_time, open, high, low, close, volume, quote_volume, is_final)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol, open_time)
            DO UPDATE SET
              open = EXCLUDED.open,
              high = EXCLUDED.high,
              low = EXCLUDED.low,
              close = EXCLUDED.close,
              volume = EXCLUDED.volume,
              quote_volume = EXCLUDED.quote_volume,
              is_final = EXCLUDED.is_final
            """;

        try {
            int rows = jdbcTemplate.update(sql,
                    k.getSymbol(),
                    new Timestamp(k.getOpenTime()),
                    k.getOpen(),
                    k.getHigh(),
                    k.getLow(),
                    k.getClose(),
                    k.getVolume(),
                    k.getQuoteVolume(),
                    k.getIsFinal() != null && k.getIsFinal()
            );
            log.debug("K线UPSERT成功: symbol={}, time={}, rows={}",
                    k.getSymbol(), k.getOpenTime(), rows);
        } catch (Exception e) {
            log.error("K线UPSERT失败: symbol={}, time={}", k.getSymbol(), k.getOpenTime(), e);
            throw new RuntimeException("K线数据写入失败", e);
        }
    }

    /**
     * 批量插入或更新K线数据
     */
    public void batchUpsert(List<Kline> klines) {
        String sql = """
            INSERT INTO kline_1m(symbol, open_time, open, high, low, close, volume, quote_volume, is_final)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol, open_time)
            DO UPDATE SET
              open = EXCLUDED.open,
              high = EXCLUDED.high,
              low = EXCLUDED.low,
              close = EXCLUDED.close,
              volume = EXCLUDED.volume,
              quote_volume = EXCLUDED.quote_volume,
              is_final = EXCLUDED.is_final
            """;

        try {
            jdbcTemplate.batchUpdate(sql, klines, klines.size(), (ps, k) -> {
                ps.setString(1, k.getSymbol());
                ps.setTimestamp(2, new Timestamp(k.getOpenTime()));
                ps.setBigDecimal(3, k.getOpen());
                ps.setBigDecimal(4, k.getHigh());
                ps.setBigDecimal(5, k.getLow());
                ps.setBigDecimal(6, k.getClose());
                ps.setBigDecimal(7, k.getVolume());
                ps.setBigDecimal(8, k.getQuoteVolume());
                ps.setBoolean(9, k.getIsFinal() != null && k.getIsFinal());
            });
            log.debug("批量UPSERT成功: {} 条记录", klines.size());
        } catch (Exception e) {
            log.error("批量UPSERT失败", e);
            throw new RuntimeException("批量K线数据写入失败", e);
        }
    }

    /**
     * 获取指定symbol的最后一条K线时间戳
     */
    public Long getLastOpenTime(String symbol) {
        String sql = "SELECT MAX(open_time) FROM kline_1m WHERE symbol = ?";
        try {
            // 使用 Timestamp 类型接收数据库返回的时间戳
            Timestamp timestamp = jdbcTemplate.queryForObject(sql, Timestamp.class, symbol);
            // 如果查询结果不为空，转换为毫秒时间戳
            return timestamp != null ? timestamp.getTime() : null;
        } catch (EmptyResultDataAccessException e) {
            // 没有数据时返回 null（不打印错误日志）
            return null;
        } catch (Exception e) {
            log.error("查询最后时间戳失败: symbol={}", symbol, e);
            return null;
        }
    }


    /**
     * 获取最新的N条K线数据
     */
    public List<Kline> findLatest(String symbol, int limit) {
        String sql = """
            SELECT * FROM kline_1m 
            WHERE symbol = ? 
            ORDER BY open_time DESC 
            LIMIT ?
            """;
        try {
            return jdbcTemplate.query(sql,
                    new Object[]{symbol, limit},
                    (rs, rowNum) -> mapRowToKline(rs));
        } catch (Exception e) {
            log.error("查询最新K线失败: symbol={}, limit={}", symbol, limit, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询指定时间范围内的K线数据
     */
    public List<Kline> findByTimeRange(String symbol, long startTime, long endTime) {
        String sql = """
            SELECT * FROM kline_1m 
            WHERE symbol = ? 
            AND open_time BETWEEN ? AND ?
            ORDER BY open_time ASC
            """;
        try {
            return jdbcTemplate.query(sql,
                    new Object[]{
                            symbol,
                            new Timestamp(startTime),
                            new Timestamp(endTime)
                    },
                    (rs, rowNum) -> mapRowToKline(rs));
        } catch (Exception e) {
            log.error("查询时间范围K线失败: symbol={}, start={}, end={}",
                    symbol, startTime, endTime, e);
            return new ArrayList<>();
        }
    }

    /**
     * 统计指定symbol的K线总数
     */
    public long count(String symbol) {
        String sql = "SELECT COUNT(*) FROM kline_1m WHERE symbol = ?";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, symbol);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计K线数量失败: symbol={}", symbol, e);
            return 0;
        }
    }

    /**
     * 删除指定时间之前的K线数据（数据清理）
     */
    public int deleteBefore(String symbol, long beforeTime) {
        String sql = "DELETE FROM kline_1m WHERE symbol = ? AND open_time < ?";
        try {
            int rows = jdbcTemplate.update(sql, symbol, new Timestamp(beforeTime));
            log.info("清理K线数据: symbol={}, before={}, rows={}",
                    symbol, new Timestamp(beforeTime), rows);
            return rows;
        } catch (Exception e) {
            log.error("清理K线数据失败: symbol={}, before={}", symbol, beforeTime, e);
            return 0;
        }
    }

    /**
     * 结果集映射为Kline对象
     */
    private Kline mapRowToKline(ResultSet rs) throws java.sql.SQLException {
        Kline k = new Kline();
        k.setSymbol(rs.getString("symbol"));
        k.setOpenTime(rs.getTimestamp("open_time").getTime());
        k.setOpen(rs.getBigDecimal("open"));
        k.setHigh(rs.getBigDecimal("high"));
        k.setLow(rs.getBigDecimal("low"));
        k.setClose(rs.getBigDecimal("close"));
        k.setVolume(rs.getBigDecimal("volume"));
        k.setQuoteVolume(rs.getBigDecimal("quote_volume"));
        k.setIsFinal(rs.getBoolean("is_final"));
        return k;
    }
}
