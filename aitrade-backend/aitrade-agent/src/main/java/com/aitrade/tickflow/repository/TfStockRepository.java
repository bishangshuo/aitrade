package com.aitrade.tickflow.repository;

import com.aitrade.tickflow.domain.TfStock;
import com.aitrade.tickflow.domain.TfStockExt;
import com.aitrade.tickflow.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class TfStockRepository {

    private static final String TABLE = "tf_stock";

    private static final String COLUMNS =
            "symbol, exchange, code, name, region, type, ext";

    private static final RowMapper<TfStock> ROW_MAPPER = (rs, rowNum) -> mapStock(rs);

    private final JdbcTemplate jdbcTemplate;

    public TfStockRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // 新增 / Upsert
    // ============================================================

    /**
     * 保存单条（存在则更新）
     */
    public int save(TfStock stock) {
        String sql = """
                INSERT INTO tf_stock
                    (symbol, exchange, code, name, region, type, ext)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol)
                DO UPDATE SET
                    exchange = EXCLUDED.exchange,
                    code     = EXCLUDED.code,
                    name     = EXCLUDED.name,
                    region   = EXCLUDED.region,
                    type     = EXCLUDED.type,
                    ext      = EXCLUDED.ext
                """;

        return jdbcTemplate.update(
                sql,
                stock.getSymbol(),
                stock.getExchange(),
                stock.getCode(),
                stock.getName(),
                stock.getRegion(),
                stock.getType(),
                JsonUtils.toJsonb(stock.getExt())
        );
    }

    /**
     * 批量保存（存在则更新）
     */
    public int batchSave(List<TfStock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO tf_stock
                    (symbol, exchange, code, name, region, type, ext)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol)
                DO UPDATE SET
                    exchange = EXCLUDED.exchange,
                    code     = EXCLUDED.code,
                    name     = EXCLUDED.name,
                    region   = EXCLUDED.region,
                    type     = EXCLUDED.type,
                    ext      = EXCLUDED.ext
                """;

        int[][] result = jdbcTemplate.batchUpdate(
                sql,
                stocks,
                1000,
                (ps, stock) -> {
                    ps.setString(1, stock.getSymbol());
                    ps.setString(2, stock.getExchange());
                    ps.setString(3, stock.getCode());
                    ps.setString(4, stock.getName());
                    ps.setString(5, stock.getRegion());
                    ps.setString(6, stock.getType());
                    ps.setObject(7, JsonUtils.toJsonb(stock.getExt()));
                }
        );

        return Arrays.stream(result)
                .flatMapToInt(Arrays::stream)
                .sum();
    }

    // ============================================================
    // 删除
    // ============================================================

    public int deleteBySymbol(String symbol) {
        return jdbcTemplate.update(
                "DELETE FROM tf_stock WHERE symbol = ?", symbol);
    }

    public int deleteBySymbols(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return 0;
        }
        String sql = "DELETE FROM tf_stock WHERE symbol = ANY (?)";
        return jdbcTemplate.update(sql,
                ps -> ps.setArray(1,
                        ps.getConnection().createArrayOf("varchar", symbols.toArray())));
    }

    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM tf_stock");
    }

    // ============================================================
    // 修改（局部更新用 update；整体替换用 save）
    // ============================================================

    /**
     * 按 symbol 全量更新（symbol 不可改）
     */
    public int update(TfStock stock) {
        String sql = """
                UPDATE tf_stock
                   SET exchange = ?,
                       code     = ?,
                       name     = ?,
                       region   = ?,
                       type     = ?,
                       ext      = ?
                 WHERE symbol   = ?
                """;

        return jdbcTemplate.update(
                sql,
                stock.getExchange(),
                stock.getCode(),
                stock.getName(),
                stock.getRegion(),
                stock.getType(),
                JsonUtils.toJsonb(stock.getExt()),
                stock.getSymbol()
        );
    }

    // ============================================================
    // 查询
    // ============================================================

    public Optional<TfStock> findBySymbol(String symbol) {
        String sql = "SELECT " + COLUMNS + " FROM tf_stock WHERE symbol = ?";
        List<TfStock> list = jdbcTemplate.query(sql, ROW_MAPPER, symbol);
        return list.stream().findFirst();
    }

    public List<TfStock> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM tf_stock ORDER BY symbol";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<TfStock> findByExchange(String exchange) {
        String sql = "SELECT " + COLUMNS
                + " FROM tf_stock WHERE exchange = ? ORDER BY symbol";
        return jdbcTemplate.query(sql, ROW_MAPPER, exchange);
    }

    public List<TfStock> findByType(String type) {
        String sql = "SELECT " + COLUMNS
                + " FROM tf_stock WHERE type = ? ORDER BY symbol";
        return jdbcTemplate.query(sql, ROW_MAPPER, type);
    }

    /**
     * 按名称模糊匹配
     */
    public List<TfStock> findByNameLike(String keyword) {
        String sql = "SELECT " + COLUMNS
                + " FROM tf_stock WHERE name ILIKE ? ORDER BY symbol";
        return jdbcTemplate.query(sql, ROW_MAPPER, "%" + keyword + "%");
    }

    /**
     * 分页查询
     */
    public List<TfStock> findPage(int offset, int limit) {
        String sql = "SELECT " + COLUMNS
                + " FROM tf_stock ORDER BY symbol OFFSET ? LIMIT ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, offset, limit);
    }

    public boolean existsBySymbol(String symbol) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tf_stock WHERE symbol = ?",
                Integer.class, symbol);
        return count != null && count > 0;
    }

    public long count() {
        Long c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tf_stock", Long.class);
        return c == null ? 0L : c;
    }

    // ============================================================
    // 行映射
    // ============================================================

    private static TfStock mapStock(ResultSet rs) throws SQLException {
        TfStock s = new TfStock();
        s.setSymbol(rs.getString("symbol"));
        s.setExchange(rs.getString("exchange"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setRegion(rs.getString("region"));
        s.setType(rs.getString("type"));
        s.setExt(JsonUtils.fromJson(rs.getString("ext"), TfStockExt.class));
        return s;
    }
}