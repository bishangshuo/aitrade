package com.aitrade.exchange.controller;

import com.aitrade.exchange.component.SymbolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 交易对管理 API
 * 提供动态增删交易对的 REST 接口
 */
@RestController
@RequestMapping("/api/symbols")
@Slf4j
public class SymbolController {

    @Autowired
    private SymbolManager symbolManager;

    /**
     * 获取所有活跃交易对
     * GET /api/symbols
     */
    @GetMapping
    public ResponseEntity<Set<String>> listSymbols() {
        return ResponseEntity.ok(symbolManager.getActiveSymbols());
    }

    /**
     * 添加交易对
     * POST /api/symbols?symbol=ETH-USDT
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addSymbol(@RequestParam String symbol) {
        Map<String, Object> result = new HashMap<>();
        // 参数校验
        if (symbol == null || !symbol.matches("^[A-Z]+-[A-Z]+$")) {
            result.put("success", false);
            result.put("message", "交易对格式无效，应为 XXX-YYY 格式，如 BTC-USDT");
            return ResponseEntity.badRequest().body(result);
        }
        boolean added = symbolManager.addSymbol(symbol);
        result.put("success", added);
        result.put("message", added ? "交易对已添加: " + symbol : "交易对已存在: " + symbol);
        result.put("activeSymbols", symbolManager.getActiveSymbols());
        return ResponseEntity.ok(result);
    }

    /**
     * 移除交易对
     * DELETE /api/symbols?symbol=BTC-USDT
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> removeSymbol(@RequestParam String symbol) {
        Map<String, Object> result = new HashMap<>();
        boolean removed = symbolManager.removeSymbol(symbol);
        result.put("success", removed);
        result.put("message", removed ? "交易对已移除: " + symbol : "交易对不存在: " + symbol);
        result.put("activeSymbols", symbolManager.getActiveSymbols());
        return ResponseEntity.ok(result);
    }

    /**
     * 批量添加交易对
     * POST /api/symbols/batch?symbols=BTC-USDT,ETH-USDT,SOL-USDT
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchAdd(@RequestParam String symbols) {
        Map<String, Object> result = new HashMap<>();
        String[] arr = symbols.split(",");
        int added = 0;
        for (String s : arr) {
            s = s.trim();
            if (s.matches("^[A-Z]+-[A-Z]+$") && symbolManager.addSymbol(s)) {
                added++;
            }
        }
        result.put("success", true);
        result.put("added", added);
        result.put("activeSymbols", symbolManager.getActiveSymbols());
        return ResponseEntity.ok(result);
    }
}
