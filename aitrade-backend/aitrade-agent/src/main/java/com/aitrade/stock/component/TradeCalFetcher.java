package com.aitrade.stock.component;

import com.aitrade.common.utils.DateConvertUtils;
import com.aitrade.stock.domain.TaCal;
import com.aitrade.stock.mapper.TaCalMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TradeCalFetcher {

    private static final Logger log = LoggerFactory.getLogger(TradeCalFetcher.class);

    /** 首次全量起始年月 */
    private static final YearMonth FULL_SYNC_START = YearMonth.of(2010, 1);
    /** 增量向前最多回溯月数，防止异常数据导致死循环 */
    private static final int MAX_LOOKBACK_MONTHS = 240;

    @Resource
    private TaCalMapper taCalMapper;
    @Resource
    private SzseCalendarClient szseCalendarClient;

    /** 已存在的日期（内存缓存，用 LocalDate 便于比较） */
    private final Set<LocalDate> existingDates = ConcurrentHashMap.newKeySet();
    /** 已同步的月份 */
    private final Set<YearMonth> syncedMonths = ConcurrentHashMap.newKeySet();

    private volatile boolean initialized = false;

    //@Scheduled(fixedRate = 5000)
    public void fetchTradeCal() {
        try {
            synchronized (this) {
                if (!initialized) {
                    initFromDatabase();
                    initialized = true;
                }
                incrementalSync();
            }
        } catch (Exception e) {
            log.error("fetchTradeCal error", e);
        }
    }

    /**
     * 从数据库加载已有日历，构建内存缓存；若库为空则触发首次全量。
     */
    private void initFromDatabase() {
        log.info("初始化交易日历缓存...");
        List<TaCal> all = taCalMapper.selectTaCalList(new TaCal());
        for (TaCal cal : all) {
            LocalDate ld = DateConvertUtils.toLocalDate(cal.getCalDate());
            if (ld == null) continue;
            existingDates.add(ld);
            syncedMonths.add(YearMonth.from(ld));
        }
        log.info("已加载 {} 条日历记录，覆盖 {} 个月份",
                existingDates.size(), syncedMonths.size());

        if (all.isEmpty()) {
            log.info("数据库为空，执行首次全量同步 {} ~ {}",
                    FULL_SYNC_START, YearMonth.now());
            fullSync();
        }
    }

    /**
     * 首次全量：从 2010-01 逐月同步到当前月。
     */
    private void fullSync() {
        YearMonth end = YearMonth.now();
        YearMonth cur = FULL_SYNC_START;
        int months = 0;
        while (!cur.isAfter(end)) {
            if (!syncedMonths.contains(cur)) {
                syncMonth(cur);
                months++;
            }
            cur = cur.plusMonths(1);
        }
        log.info("全量同步完成，共同步 {} 个月份，累计 {} 条记录",
                months, existingDates.size());
    }

    /**
     * 增量同步：从当前月逐月往前推，遇到已同步的月份即停止。
     */
    private void incrementalSync() {
        YearMonth cur = YearMonth.now();
        int lookback = 0;
        while (lookback++ < MAX_LOOKBACK_MONTHS) {
            if (syncedMonths.contains(cur)) {
                break;  // 碰到数据库中已存在的月份，停止
            }
            syncMonth(cur);
            cur = cur.minusMonths(1);
        }
    }

    /**
     * 同步单个月份：抓取接口数据，跳过已存在日期后逐条插入。
     */
    private void syncMonth(YearMonth ym) {
        log.info("开始同步月份: {}", ym);
        List<TaCal> list;
        try {
            list = szseCalendarClient.fetchMonth(ym);
        } catch (Exception e) {
            log.error("抓取月份 {} 失败，稍后重试", ym, e);
            return;
        }

        int inserted = 0;
        for (TaCal cal : list) {
            LocalDate ld = DateConvertUtils.toLocalDate(cal.getCalDate());
            if (ld == null || existingDates.contains(ld)) continue;
            try {
                taCalMapper.insertTaCal(cal);
                existingDates.add(ld);
                inserted++;
            } catch (Exception e) {
                log.warn("插入日历失败: {}", ld, e);
            }
        }
        syncedMonths.add(ym);
        log.info("月份 {} 同步完成，接口返回 {} 条，新增插入 {} 条",
                ym, list.size(), inserted);

        try {
            Thread.sleep(300);  // 控制请求频率
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}