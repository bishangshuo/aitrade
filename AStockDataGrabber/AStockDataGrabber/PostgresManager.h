#pragma once

#include <string>
#include <mutex>
#include <memory>
#include <pqxx/pqxx>
#include <iostream>
#include <nlohmann/json.hpp> 

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

	// 批量插入日K线数据
	bool BatchInsertKlineDay(const std::string& stCode, const std::string& stName, int marketType, const std::vector<std::string>& rawDataList);

	// 批量插入资产负债表数据
	bool InsertBalanceSheet(const nlohmann::json& jsonData);

	//批量插入利润表数据
	bool InsertIncomeStatement(const nlohmann::json& jsonData);

	//批量插入现金流量表数据
	bool InsertCashFlowStatement(const nlohmann::json& jsonData);

private:
	PostgresManager();
	~PostgresManager();

	// 檢查並嘗試自動恢復連線
	bool CheckConnection();

private:
	std::string m_connStr;
	std::unique_ptr<pqxx::connection> m_conn;
	bool m_isConnected{ false };
	std::mutex m_mutex;
};
