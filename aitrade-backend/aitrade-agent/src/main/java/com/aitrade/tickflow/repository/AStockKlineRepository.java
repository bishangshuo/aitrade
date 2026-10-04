package com.aitrade.tickflow.repository;

import com.aitrade.tickflow.domain.TfKline;
import com.aitrade.tickflow.domain.TfStock;
import com.aitrade.tickflow.enums.TfKlinePeriod;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class AStockKlineRepository {

    private final JdbcTemplate jdbcTemplate;

    public AStockKlineRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存单条K线
     */
    public int save(TfKlinePeriod period, TfKline kline) {

        String tableName = period.getTableName();

        String sql = """
                INSERT INTO %s
                (symbol, stock_name, timestamp, open, high, low, close, volume, amount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol, timestamp)
                DO UPDATE SET
                    open = EXCLUDED.open,
                    high = EXCLUDED.high,
                    low = EXCLUDED.low,
                    close = EXCLUDED.close,
                    volume = EXCLUDED.volume,
                    amount = EXCLUDED.amount
                """.formatted(tableName);

        return jdbcTemplate.update(
                sql,
                kline.getSymbol(),
                kline.getStockName(),
                toTimestamp(kline.getTimestamp()),
                kline.getOpen(),
                kline.getHigh(),
                kline.getLow(),
                kline.getClose(),
                kline.getVolume(),
                kline.getAmount()
        );
    }

    /**
     * 批量保存K线
     */
    public int batchSave(TfKlinePeriod period, List<TfKline> klines) {

        if (klines == null || klines.isEmpty()) {
            return 0;
        }

        String tableName = period.getTableName();

        String sql = """
            INSERT INTO %s
            (symbol, stock_name, timestamp, open, high, low, close, volume, amount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol, timestamp)
            DO UPDATE SET
                open = EXCLUDED.open,
                high = EXCLUDED.high,
                low = EXCLUDED.low,
                close = EXCLUDED.close,
                volume = EXCLUDED.volume,
                amount = EXCLUDED.amount
            """.formatted(tableName);

        int[][] result = jdbcTemplate.batchUpdate(
                sql,
                klines,
                100000,
                (ps, kline) -> {
                    ps.setString(1, kline.getSymbol());
                    ps.setString(2, kline.getStockName());
                    ps.setTimestamp(3, toTimestamp(kline.getTimestamp()));
                    ps.setBigDecimal(4, kline.getOpen());
                    ps.setBigDecimal(5, kline.getHigh());
                    ps.setBigDecimal(6, kline.getLow());
                    ps.setBigDecimal(7, kline.getClose());
                    ps.setBigDecimal(8, kline.getVolume());
                    ps.setBigDecimal(9, kline.getAmount());
                }
        );

        return Arrays.stream(result)
                .flatMapToInt(Arrays::stream)
                .sum();
    }

    /**
     * 查询某只股票K线
     */
    public List<TfKline> findBySymbol(
            TfKlinePeriod period,
            String symbol,
            long startTimestamp,
            long endTimestamp) {

        String tableName = period.getTableName();

        String sql = """
                SELECT
                    symbol,
                    stock_name,
                    timestamp,
                    open,
                    high,
                    low,
                    close,
                    volume,
                    amount
                FROM %s
                WHERE symbol = ?
                  AND timestamp >= ?
                  AND timestamp <= ?
                ORDER BY timestamp ASC
                """.formatted(tableName);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapKline(rs),
                symbol,
                toTimestamp(startTimestamp),
                toTimestamp(endTimestamp)
        );
    }

    /**
     * 获取一只股票的所有K线
     */
    public List<TfKline> findBySymbol(
            TfKlinePeriod period,
            String symbol) {
        String tableName = period.getTableName();

        String sql = """
                SELECT
                    symbol,
                    stock_name,
                    timestamp,
                    open,
                    high,
                    low,
                    close,
                    volume,
                    amount
                FROM %s
                WHERE symbol = ?
                ORDER BY timestamp ASC
                """.formatted(tableName);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapKline(rs),
                symbol
        );
    }

    /**
     * 查询某只股票最近N条K线
     */
    public List<TfKline> findLatest(
            TfKlinePeriod period,
            String symbol,
            int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        String tableName = period.getTableName();

        String sql = """
                SELECT
                    symbol,
                    stock_name,
                    timestamp,
                    open,
                    high,
                    low,
                    close,
                    volume,
                    amount
                FROM %s
                WHERE symbol = ?
                ORDER BY timestamp DESC
                LIMIT ?
                """.formatted(tableName);

        List<TfKline> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapKline(rs),
                symbol,
                limit
        );

        // 数据库是DESC查询，业务层通常希望拿到正序数据
        Collections.reverse(result);

        return result;
    }

    /**
     * 查询最新一条K线
     */
    public TfKline findLatest(
            TfKlinePeriod period,
            String symbol) {

        String tableName = period.getTableName();

        String sql = """
                SELECT
                    symbol,
                    stock_name,
                    timestamp,
                    open,
                    high,
                    low,
                    close,
                    volume,
                    amount
                FROM %s
                WHERE symbol = ?
                ORDER BY timestamp DESC
                LIMIT 1
                """.formatted(tableName);

        List<TfKline> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapKline(rs),
                symbol
        );

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * 查询某只股票是否存在指定时间的K线
     */
    public boolean exists(
            TfKlinePeriod period,
            String symbol,
            long timestamp) {

        String tableName = period.getTableName();

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM %s
                    WHERE symbol = ?
                      AND timestamp = ?
                )
                """.formatted(tableName);

        Boolean result = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                symbol,
                toTimestamp(timestamp)
        );

        return Boolean.TRUE.equals(result);
    }

    /**
     * 删除某只股票指定时间范围的数据
     */
    public int delete(
            TfKlinePeriod period,
            String symbol,
            long startTimestamp,
            long endTimestamp) {

        String tableName = period.getTableName();

        String sql = """
                DELETE FROM %s
                WHERE symbol = ?
                  AND timestamp >= ?
                  AND timestamp <= ?
                """.formatted(tableName);

        return jdbcTemplate.update(
                sql,
                symbol,
                toTimestamp(startTimestamp),
                toTimestamp(endTimestamp)
        );
    }

    /**
     * 查询指定股票最新K线时间
     */
    public Long findLatestTimestamp(
            TfKlinePeriod period,
            String symbol) {

        String tableName = period.getTableName();

        String sql = """
                SELECT EXTRACT(EPOCH FROM timestamp) * 1000
                FROM %s
                WHERE symbol = ?
                ORDER BY timestamp DESC
                LIMIT 1
                """.formatted(tableName);

        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong(1),
                symbol
        );

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * ResultSet → TfKline
     */
    private TfKline mapKline(ResultSet rs) throws SQLException {

        TfKline kline = new TfKline();

        kline.setSymbol(rs.getString("symbol"));
        kline.setStockName(rs.getString("stock_name"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            kline.setTimestamp(timestamp.getTime());
        }

        kline.setOpen(rs.getBigDecimal("open"));
        kline.setHigh(rs.getBigDecimal("high"));
        kline.setLow(rs.getBigDecimal("low"));
        kline.setClose(rs.getBigDecimal("close"));
        kline.setVolume(rs.getBigDecimal("volume"));
        kline.setAmount(rs.getBigDecimal("amount"));

        return kline;
    }

    private TfStock mapStock(ResultSet rs) throws SQLException {
        TfStock stock = new TfStock();
        stock.setSymbol(rs.getString("symbol"));
        stock.setName(rs.getString("stock_name"));
        return stock;
    }

    /**
     * Java Unix毫秒时间戳 → PostgreSQL Timestamp
     */
    private Timestamp toTimestamp(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Timestamp.from(Instant.ofEpochMilli(timestamp));
    }
}