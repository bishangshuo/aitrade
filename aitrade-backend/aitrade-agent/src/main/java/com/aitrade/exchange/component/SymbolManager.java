package com.aitrade.exchange.component;
import com.aitrade.exchange.domain.AtInst;
import com.aitrade.exchange.event.SymbolChangeEvent;
import com.aitrade.exchange.mapper.AtInstMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 交易对注册中心
 * - 活跃交易对列表持久化在 Redis Set 中（重启不丢失）
 * - 增删时发布 SymbolChangeEvent，通知 OkxWsClient 和 CompensationTask
 */
@Component
@Slf4j
public class SymbolManager {

    private static final String ACTIVE_SYMBOLS_KEY = "symbols:active";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private AtInstMapper atInstMapper;

    /**
     * 添加交易对
     * @return true=新增成功，false=已存在
     */
    public boolean addSymbol(String symbol) {
        Long added = redisTemplate.opsForSet().add(ACTIVE_SYMBOLS_KEY, symbol);
        if (added != null && added > 0) {
            log.info("新增交易对: {}", symbol);
            eventPublisher.publishEvent(new SymbolChangeEvent(this, symbol, SymbolChangeEvent.ActionType.ADD));
            return true;
        }
        log.debug("交易对已存在: {}", symbol);
        return false;
    }

    /**
     * 移除交易对
     * @return true=移除成功，false=不存在
     */
    public boolean removeSymbol(String symbol) {
        Long removed = redisTemplate.opsForSet().remove(ACTIVE_SYMBOLS_KEY, symbol);
        if (removed != null && removed > 0) {
            log.info("移除交易对: {}", symbol);
            eventPublisher.publishEvent(new SymbolChangeEvent(this, symbol, SymbolChangeEvent.ActionType.REMOVE));
            return true;
        }
        log.debug("交易对不存在: {}", symbol);
        return false;
    }

    /**
     * 获取所有活跃交易对
     */
    public Set<String> getActiveSymbols() {
        Set<String> symbols = redisTemplate.opsForSet().members(ACTIVE_SYMBOLS_KEY);
        return symbols != null ? symbols : Collections.emptySet();
    }

    /**
     * 判断交易对是否活跃
     */
    public boolean isActive(String symbol) {
        Boolean isMember = redisTemplate.opsForSet().isMember(ACTIVE_SYMBOLS_KEY, symbol);
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 初始化默认交易对（仅在 Redis 中无数据时执行）
     */
    public void initDefaultsIfEmpty() {
        //从数据库加载在监交易对
        List<AtInst> listAtInst = atInstMapper.selectAtInstList(new AtInst());
        if(listAtInst != null) {
            for(AtInst atInst: listAtInst) {
                if(atInst.getWatch().equals(1) && atInst.getStatus().equals(1)) {
                    addSymbol(atInst.getInstId());
                } else {
                    removeSymbol(atInst.getInstId());
                }
            }
        }
        if (getActiveSymbols().isEmpty()) {
            log.info("未发现活跃交易对，初始化默认: BTC-USDT");
            addSymbol("BTC-USDT");
        }
    }
}
