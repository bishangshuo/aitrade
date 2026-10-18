package com.aitrade.stock.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
CREATE TABLE `ta_stock_pick_diagnostic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
  `symbol` VARCHAR(20) NOT NULL COMMENT '股票代碼',
  `stock_name` VARCHAR(50) DEFAULT NULL COMMENT '股票名稱',
  `trade_date` DATE NOT NULL COMMENT '交易日期',
  `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名稱',

  `passed` TINYINT(1) NOT NULL COMMENT '是否通過篩選 (1:通過, 0:未通過)',
  `fail_reason` VARCHAR(50) DEFAULT NULL COMMENT '未通過時的攔截原因',

  `match_vcp` TINYINT(1) DEFAULT 0 COMMENT '是否符合VCP形態',
  `match_ma_coil` TINYINT(1) DEFAULT 0 COMMENT '是否符合均線粘合形態',
  `match_air_refuel` TINYINT(1) DEFAULT 0 COMMENT '是否符合空中加油形態',

  `close_price` DECIMAL(10,2) DEFAULT NULL COMMENT '當日收盤價',
  `change_rate` DECIMAL(6,4) DEFAULT NULL COMMENT '當日漲跌幅',
  `vol_ratio` DECIMAL(8,4) DEFAULT NULL COMMENT '放量倍數',
  `vcp_amplitude` DECIMAL(6,4) DEFAULT NULL COMMENT 'VCP前期10日振幅',
  `macd_dif` DECIMAL(10,4) DEFAULT NULL COMMENT 'MACD DIF',
  `macd_dea` DECIMAL(10,4) DEFAULT NULL COMMENT 'MACD DEA',
  `macd_hist` DECIMAL(10,4) DEFAULT NULL COMMENT 'MACD柱狀體',
  `kdj_k` DECIMAL(8,2) DEFAULT NULL COMMENT 'KDJ K值',
  `kdj_d` DECIMAL(8,2) DEFAULT NULL COMMENT 'KDJ D值',
  `rsi14` DECIMAL(8,2) DEFAULT NULL COMMENT 'RSI 14數值',
  `obv` DECIMAL(20,4) DEFAULT NULL COMMENT 'OBV能量潮數值',
  `profit_ratio` DECIMAL(6,4) DEFAULT NULL COMMENT '獲利盤比例',
  `is_chip_single_peak` TINYINT(1) DEFAULT NULL COMMENT '是否籌碼單峰密集',

  `volume_score` DECIMAL(6,2) DEFAULT NULL COMMENT '量價爆發力得分',
  `chip_score` DECIMAL(6,2) DEFAULT NULL COMMENT '籌碼面得分',
  `momentum_score` DECIMAL(6,2) DEFAULT NULL COMMENT '技術動能得分',
  `pattern_score` DECIMAL(6,2) DEFAULT NULL COMMENT '形態品質得分',
  `total_score` DECIMAL(6,2) DEFAULT NULL COMMENT '綜合總得分',

  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '記錄創建時間',

  PRIMARY KEY (`id`),
  KEY `idx_date_passed` (`trade_date`, `passed`),
  KEY `idx_symbol_date` (`symbol`, `trade_date`),
  KEY `idx_fail_reason` (`fail_reason`),
  KEY `idx_total_score` (`total_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='選股策略診斷與回測明細表';
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPickDiagnostic {
    private Long id;
    // 股票代碼
    private String symbol;
    // 股票名稱
    private String stockName;
    // 交易日期
    private LocalDate tradeDate;
    // 策略代碼
    private Integer strategyCode;
    // 策略名稱
    private String strategyName;

    // 是否通過篩選(1:通過, 0:未通過)
    private Integer passed;
    // 未通過時的攔截原
    private String failReason;

    // 是否符合VCP形態
    private Boolean matchVcp;
    // 是否符合均線粘合形態
    private Boolean matchMaCoil;
    // 是否符合空中加油形態
    private Boolean matchAirRefuel;

    // 当日收盤價
    private BigDecimal closePrice;
    // 当日漲跌幅
    private BigDecimal changeRate;
    // 放量倍數
    private BigDecimal volRatio;
    // VCP前期10日振幅
    private BigDecimal vcpAmplitude;
    // MACD DIF
    private BigDecimal macdDif;
    // MACD DEA
    private BigDecimal macdDea;
    // MACD柱狀體
    private BigDecimal macdHist;
    // KDJ K值
    private BigDecimal kdjK;
    // KDJ D值
    private BigDecimal kdjD;
    // RSI 14數值
    private BigDecimal rsi14;
    // OBV能量潮數值
    private BigDecimal obv;
    // 獲利盤比例
    private BigDecimal profitRatio;
    // 是否籌碼單峰密集
    private Boolean isChipSinglePeak;

    // 量價爆發力得分
    private BigDecimal volumeScore;
    // 筹碼面得分
    private BigDecimal chipScore;
    // 技術動能得分
    private BigDecimal momentumScore;
    // 形態品質得分
    private BigDecimal patternScore;
    // 綜合總得分
    private BigDecimal totalScore;

    // 記錄創建時間
    private LocalDateTime createdAt;
}