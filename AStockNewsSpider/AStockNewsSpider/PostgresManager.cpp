#include "pch.h"
#include "PostgresManager.h"
#include <iostream>

PostgresManager::PostgresManager()
	: m_isConnected(false)
{
}

PostgresManager::~PostgresManager()
{
	std::lock_guard<std::mutex> lock(m_mutex);
	if (m_conn && m_conn->is_open())
	{
		try {
			m_conn->close();
		}
		catch (...) {
			// 忽略析構時的異常
		}
	}
}

bool PostgresManager::Initialize(const std::string& connectionString)
{
	std::lock_guard<std::mutex> lock(m_mutex);
	m_connStr = connectionString;

	try {
		m_conn = std::make_unique<pqxx::connection>(m_connStr);
		if (m_conn->is_open()) {
			m_isConnected = true;

			// 自動建立資料表結構
			if (!CreateTableIfNotExist()) {
				std::cerr << "[PostgresManager] 自動建立資料表結構失敗。" << std::endl;
				return false;
			}

			std::cout << "[PostgresManager] PostgreSQL 資料庫初始化成功。" << std::endl;
			return true;
		}
		else {
			std::cerr << "[PostgresManager] 無法開啟 PostgreSQL 資料庫連線。" << std::endl;
			m_isConnected = false;
			return false;
		}
	}
	catch (const std::exception& e) {
		std::cerr << "[PostgresManager] 連線例外: " << e.what() << std::endl;
		m_isConnected = false;
		return false;
	}
}

bool PostgresManager::CheckConnection()
{
	if (m_conn && m_conn->is_open() && m_isConnected)
	{
		return true;
	}

	std::cout << "[PostgresManager] 檢測到連線中斷，嘗試重新連線..." << std::endl;
	try {
		m_conn = std::make_unique<pqxx::connection>(m_connStr);
		if (m_conn->is_open()) {
			m_isConnected = true;
			std::cout << "[PostgresManager] 重連成功！" << std::endl;
			return true;
		}
	}
	catch (const std::exception& e) {
		std::cerr << "[PostgresManager] 重連失敗: " << e.what() << std::endl;
	}

	m_isConnected = false;
	return false;
}

bool PostgresManager::CreateTableIfNotExist()
{
	if (!m_conn || !m_conn->is_open()) return false;

	try {
		pqxx::work txn(*m_conn);

		// 1. 建立基本公告表结构
		std::string createTableSql = R"(
			CREATE TABLE IF NOT EXISTS stock_news (
				stock_code VARCHAR(16) NOT NULL,
				stock_name VARCHAR(64),
				market_code VARCHAR(16),
				ann_type VARCHAR(32),
				source_type VARCHAR(32),
				art_code VARCHAR(64) NOT NULL,
				publish_time TIMESTAMP WITH TIME ZONE NOT NULL,
				title TEXT,
				content TEXT,
				fileUrl TEXT,
				created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
				PRIMARY KEY (art_code, publish_time)
			);
		)";
		txn.exec(createTableSql);

		// 2. 為 art_code 建立唯一索引，確保 ON CONFLICT (art_code) 語法生效
		std::string createIndexSql = R"(
			CREATE UNIQUE INDEX IF NOT EXISTS idx_stock_news_art_code ON stock_news (publish_time, art_code);
		)";
		txn.exec(createIndexSql);

		// 3. 嘗試設定為 TimescaleDB Hypertable（若無擴充元件則自動忽略例外）
		try {
			std::string createHypertableSql = R"(
				SELECT create_hypertable('stock_news', 'publish_time', if_not_exists => TRUE);
			)";
			txn.exec(createHypertableSql);
		}
		catch (...) {
			// 普通 PostgreSQL 環境下忽視此錯誤
		}

		txn.commit();
		return true;
	}
	catch (const std::exception& e) {
		std::cerr << "[PostgresManager] CreateTableIfNotExist 失敗: " << e.what() << std::endl;
		return false;
	}
}

bool PostgresManager::InsertNews(
	const std::string& stockCode,
	const std::string& stockName,
	const std::string& artCode,
	const std::string& annType,
	const std::string& marketCode,
	const std::string& sourceType,
	const std::string& publishTime,
	const std::string& title,
	const std::string& content,
	const std::string& fileUrl
)
{
	std::lock_guard<std::mutex> lock(m_mutex);

	// 檢查連線，若斷線自動嘗試重新連線
	if (!CheckConnection()) return false;

	try {
		pqxx::work txn(*m_conn);

		std::string sql = R"(
			INSERT INTO stock_news 
			(stock_code, stock_name, market_code, ann_type, source_type, art_code, publish_time, title, content, file_url) 
			VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) 
			ON CONFLICT (publish_time, art_code) DO NOTHING;
		)";

		// pqxx 7.x 顯式綁定參數，防範 SQL 注入
		pqxx::params query_params;
		query_params.append(stockCode);
		query_params.append(stockName);
		query_params.append(marketCode);
		query_params.append(annType);
		query_params.append(sourceType);
		query_params.append(artCode);
		query_params.append(publishTime);
		query_params.append(title);
		query_params.append(content);
		query_params.append(fileUrl);

		txn.exec(sql, query_params);

		txn.commit();
		return true;
	}
	catch (const std::exception& e) {
		std::cerr << "[PostgresManager] InsertNews 執行失敗 (artCode: " << artCode << "): " << e.what() << std::endl;
		m_isConnected = false; // 標記連線失效，下次呼叫時自動重連
		return false;
	}
}