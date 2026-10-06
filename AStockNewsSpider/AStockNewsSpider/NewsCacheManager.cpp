#include "pch.h"
#include "NewsCacheManager.h"
#include <iostream>

NewsCacheManager::NewsCacheManager()
{
}

NewsCacheManager::~NewsCacheManager()
{
	std::lock_guard<std::mutex> lock(m_mutex);
	if (m_db)
	{
		sqlite3_close(m_db);
		m_db = nullptr;
	}
}

bool NewsCacheManager::Initialize(const std::string& dbPath)
{
	std::lock_guard<std::mutex> lock(m_mutex);

	if (m_db)
	{
		sqlite3_close(m_db);
		m_db = nullptr;
	}

	// 初始化 SQLite 資料庫連線
	if (sqlite3_open(dbPath.c_str(), &m_db) != SQLITE_OK)
	{
		if (m_db)
		{
			sqlite3_close(m_db);
			m_db = nullptr;
		}
		return false;
	}

	// 創建快取表（如果不存在）
	const char* createTableSQL = R"(
		CREATE TABLE IF NOT EXISTS news_cache (
			stock_code TEXT PRIMARY KEY,
			latest_art_code TEXT
		);
	)";

	char* errMsg = nullptr;
	if (sqlite3_exec(m_db, createTableSQL, nullptr, nullptr, &errMsg) != SQLITE_OK)
	{
		if (errMsg)
		{
			sqlite3_free(errMsg);
		}
		sqlite3_close(m_db);
		m_db = nullptr;
		return false;
	}

	return true;
}

std::string NewsCacheManager::GetLatestArtCode(const std::string& stockCode)
{
	std::lock_guard<std::mutex> lock(m_mutex);

	if (!m_db || stockCode.empty())
		return "";

	std::string latestArtCode = "";
	const char* querySQL = "SELECT latest_art_code FROM news_cache WHERE stock_code = ?;";
	sqlite3_stmt* stmt = nullptr;

	if (sqlite3_prepare_v2(m_db, querySQL, -1, &stmt, nullptr) == SQLITE_OK)
	{
		sqlite3_bind_text(stmt, 1, stockCode.c_str(), -1, SQLITE_TRANSIENT);

		if (sqlite3_step(stmt) == SQLITE_ROW)
		{
			const unsigned char* text = sqlite3_column_text(stmt, 0);
			if (text)
			{
				latestArtCode = reinterpret_cast<const char*>(text);
			}
		}
	}

	if (stmt)
	{
		sqlite3_finalize(stmt);
	}

	return latestArtCode;
}

void NewsCacheManager::UpdateLatestArtCode(const std::string& stockCode, const std::string& artCode)
{
	std::lock_guard<std::mutex> lock(m_mutex);

	if (!m_db || stockCode.empty() || artCode.empty())
		return;

	// 使用 UPSERT 語法 (主鍵衝突時自動更新)
	const char* upsertSQL = R"(
		INSERT INTO news_cache (stock_code, latest_art_code)
		VALUES (?, ?)
		ON CONFLICT(stock_code) DO UPDATE SET
			latest_art_code = excluded.latest_art_code;
	)";

	sqlite3_stmt* stmt = nullptr;

	if (sqlite3_prepare_v2(m_db, upsertSQL, -1, &stmt, nullptr) == SQLITE_OK)
	{
		sqlite3_bind_text(stmt, 1, stockCode.c_str(), -1, SQLITE_TRANSIENT);
		sqlite3_bind_text(stmt, 2, artCode.c_str(), -1, SQLITE_TRANSIENT);

		sqlite3_step(stmt);
	}

	if (stmt)
	{
		sqlite3_finalize(stmt);
	}
}