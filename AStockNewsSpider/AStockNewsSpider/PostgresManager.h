#pragma once

#include <string>
#include <mutex>
#include <memory>
#include <pqxx/pqxx>
#include <iostream>

class PostgresManager
{
public:
	static PostgresManager& Instance()
	{
		static PostgresManager instance;
		return instance;
	}

	// 禁用拷貝與賦值（單例模式）
	PostgresManager(const PostgresManager&) = delete;
	PostgresManager& operator=(const PostgresManager&) = delete;

public:
	// 初始化資料庫連線與自動建立資料表
	bool Initialize(const std::string& connectionString);

	// 插入股票公告詳情
	bool InsertNews(
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
		);

private:
	PostgresManager();
	~PostgresManager();

	// 檢查並嘗試自動恢復連線
	bool CheckConnection();

	// 初始化建表與 Hypertable 配置
	bool CreateTableIfNotExist();

private:
	std::string m_connStr;
	std::unique_ptr<pqxx::connection> m_conn;
	bool m_isConnected{ false };
	std::mutex m_mutex;
};
