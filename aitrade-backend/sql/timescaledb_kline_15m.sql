/*
 Navicat Premium Dump SQL

 Source Server         : timescaledb_kline
 Source Server Type    : PostgreSQL
 Source Server Version : 150007 (150007)
 Source Host           : 192.168.0.60:5432
 Source Catalog        : kline_db
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 150007 (150007)
 File Encoding         : 65001

 Date: 19/05/2026 22:04:20
*/


-- ----------------------------
-- Table structure for kline_15m
-- ----------------------------
DROP TABLE IF EXISTS "public"."kline_15m";
CREATE TABLE "public"."kline_15m" (
  "symbol" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "open_time" timestamptz(6) NOT NULL,
  "open" numeric(20,8),
  "high" numeric(20,8),
  "low" numeric(20,8),
  "close" numeric(20,8),
  "volume" numeric(30,8),
  "quote_volume" numeric(30,8),
  "is_final" bool DEFAULT false,
  "created_at" timestamptz(6) DEFAULT now()
);

-- ----------------------------
-- Primary Key structure for table kline_15m
-- ----------------------------
ALTER TABLE "public"."kline_15m" ADD CONSTRAINT "kline_15m_pkey" PRIMARY KEY ("symbol", "open_time");

-- 如果表还没创建为 hypertable，先执行下面这句
SELECT create_hypertable(
    'kline_15m', 
    'open_time',
    if_not_exists => TRUE,
    chunk_time_interval => INTERVAL '7 days'   -- 15分钟数据建议 chunk 间隔 7~30 天
);

-- 自动删除 3 年以前的数据
SELECT add_retention_policy(
    'kline_15m', 
    INTERVAL '3 years',           -- 保留3年
    if_not_exists => TRUE,
    schedule_interval => INTERVAL '1 day'   -- 每天检查一次（推荐）
);
