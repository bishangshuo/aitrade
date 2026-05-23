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
-- Table structure for kline_1m
-- ----------------------------
DROP TABLE IF EXISTS "public"."kline_1m";
CREATE TABLE "public"."kline_1m" (
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
)
;

-- ----------------------------
-- Primary Key structure for table kline_1m
-- ----------------------------
ALTER TABLE "public"."kline_1m" ADD CONSTRAINT "kline_1m_pkey" PRIMARY KEY ("symbol", "open_time");
