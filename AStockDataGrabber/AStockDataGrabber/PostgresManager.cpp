#include "pch.h"
#include "PostgresManager.h"
#include <iostream>
#include <sstream>
#include <iomanip>
#include <algorithm>

// ---------- 辅助函数 ----------
// 从JSON对象中取字符串值，若不存在或null则返回空字符串
static inline std::string getJsonString(const nlohmann::json& j, const std::string& key) {
    if (j.contains(key) && !j[key].is_null()) {
        if (j[key].is_string()) return j[key].get<std::string>();
        else if (j[key].is_number()) return std::to_string(j[key].get<double>());
        else if (j[key].is_boolean()) return j[key].get<bool>() ? "true" : "false";
        else return j[key].dump(); // fallback
    }
    return "";
}

// 转义SQL字符串（加单引号并转义内部引号），空字符串返回NULL
static inline std::string sqlQuote(const std::string& s) {
    if (s.empty()) return "NULL";
    std::string out;
    out.reserve(s.size() * 2 + 2);
    out.push_back('\'');
    for (char c : s) {
        if (c == '\'') out += "''";
        else if (c == '\\') out += "\\\\";
        else out += c;
    }
    out.push_back('\'');
    return out;
}

// 将JSON数值字段转为SQL数值字符串（若null或空则NULL）
static inline std::string sqlNumeric(const nlohmann::json& j, const std::string& key) {
    std::string val = getJsonString(j, key);
    if (val.empty()) return "NULL";
    return val; // 直接返回数值字符串
}


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

bool PostgresManager::BatchInsertKlineDay(const std::string& stCode, const std::string& stName, int marketType, const std::vector<std::string>& rawDataList)
{
    if (rawDataList.empty()) return true;

    std::lock_guard<std::mutex> lock(m_mutex);
    if (!CheckConnection()) {
        std::cerr << "[PostgresManager] 数据库未连接，无法插入数据。" << std::endl;
        return false;
    }

    try {
        // 1. 开启事务
        pqxx::work txn(*m_conn);

        // 2. 预分配字符串内存，拼接 SQL
        std::stringstream sqlStream;
        sqlStream << "INSERT INTO kline_day (trade_date, open, high, low, close, volume, amount, amplitude, pct_chg, change, turnover_rate, ts_code, name, market_type) VALUES ";

        size_t validCount = 0;
        for (size_t i = 0; i < rawDataList.size(); ++i) {
            const std::string& line = rawDataList[i];
            if (line.empty()) continue;

            std::stringstream ss(line);
            std::string token;
            std::vector<std::string> fields;
            while (std::getline(ss, token, ',')) {
                fields.push_back(token);
            }

            if (fields.size() < 11) continue;

            if (validCount > 0) sqlStream << ",";

            sqlStream << "('" << fields[0] << "',"     // trade_date
                << fields[1] << ","             // open
                << fields[2] << ","             // high
                << fields[3] << ","             // low
                << fields[4] << ","             // close
                << fields[5] << ","             // volume
                << fields[6] << ","             // amount
                << fields[7] << ","             // amplitude
                << fields[8] << ","             // pct_chg
                << fields[9] << ","             // change
                << fields[10] << ","            // turnover_rate
                << sqlQuote(stCode) << ","                // stCode
                << sqlQuote(stName) << ","                // stName 
                << marketType                   // market_type 
                << ")";

            validCount++;
        }

        if (validCount == 0) return true;

        // 3. 【核心改动】追加 ON CONFLICT DO NOTHING
        // 当 trade_date 发生唯一键冲突时，静默跳过该条数据，不报错，不中断事务
        sqlStream << " ON CONFLICT (trade_date, ts_code) DO NOTHING";
		std::cout << "[PostgresManager] 批量插入 SQL: " << sqlStream.str() << std::endl;

        // 4. 执行批量插入
        txn.exec(sqlStream.str());

        // 5. 统一提交事务
        txn.commit();
        std::cout << "[PostgresManager] 成功批量处理 " << validCount << " 条日K线数据（已自动忽略重复项）。" << std::endl;
        return true;
    }
    catch (const std::exception& e) {
        std::cerr << "[PostgresManager] 批量插入失败: " << e.what() << std::endl;
        return false;
    }
}

bool PostgresManager::InsertBalanceSheet(const nlohmann::json& jsonData) {
    std::lock_guard<std::mutex> lock(m_mutex);
    if (!CheckConnection()) return false;

    // 检查数据类型
    if (!jsonData.is_array() && !jsonData.is_object()) {
        std::cerr << "[PostgresManager] 无效的JSON数据，必须是对象或数组。" << std::endl;
        return false;
    }

    try {
        pqxx::work txn(*m_conn);

        // 统一转为数组处理
        nlohmann::json records;
        if (jsonData.is_object()) {
            records = nlohmann::json::array({ jsonData });
        }
        else {
            records = jsonData;
        }

        const size_t BATCH_SIZE = 1000;
        size_t total = records.size();
        for (size_t i = 0; i < total; i += BATCH_SIZE) {
            size_t end = std::min<size_t>(i + BATCH_SIZE, total);
            std::ostringstream sql;
            sql << "INSERT INTO balance_sheet ("
                << "\"SECUCODE\", \"SECURITY_CODE\", \"SECURITY_NAME_ABBR\", "
                << "\"ORG_CODE\", \"ORG_TYPE\", \"REPORT_DATE\", \"REPORT_TYPE\", "
                << "\"REPORT_DATE_NAME\", \"SECURITY_TYPE_CODE\", \"NOTICE_DATE\", "
                << "\"UPDATE_DATE\", \"CURRENCY\", "
                << "\"MONETARYFUNDS\", \"NOTE_ACCOUNTS_RECE\", \"NOTE_RECE\", "
                << "\"ACCOUNTS_RECE\", \"PREPAYMENT\", \"OTHER_RECE\", "
                << "\"INVENTORY\", \"OTHER_CURRENT_ASSET\", \"TOTAL_CURRENT_ASSETS\", "
                << "\"LONG_EQUITY_INVEST\", \"OTHER_EQUITY_INVEST\", "
                << "\"OTHER_NONCURRENT_FINASSET\", \"INVEST_REALESTATE\", "
                << "\"FIXED_ASSET\", \"CIP\", \"USERIGHT_ASSET\", "
                << "\"INTANGIBLE_ASSET\", \"LONG_PREPAID_EXPENSE\", "
                << "\"DEFER_TAX_ASSET\", \"OTHER_NONCURRENT_ASSET\", "
                << "\"TOTAL_NONCURRENT_ASSETS\", \"TOTAL_ASSETS\", "
                << "\"SHORT_LOAN\", \"NOTE_ACCOUNTS_PAYABLE\", \"NOTE_PAYABLE\", "
                << "\"ACCOUNTS_PAYABLE\", \"ADVANCE_RECEIVABLES\", \"CONTRACT_LIAB\", "
                << "\"STAFF_SALARY_PAYABLE\", \"TAX_PAYABLE\", \"OTHER_PAYABLE\", "
                << "\"DIVIDEND_PAYABLE\", \"NONCURRENT_LIAB_1YEAR\", "
                << "\"OTHER_CURRENT_LIAB\", \"TOTAL_CURRENT_LIAB\", "
                << "\"LONG_LOAN\", \"LEASE_LIAB\", \"LONG_PAYABLE\", "
                << "\"DEFER_TAX_LIAB\", \"OTHER_NONCURRENT_LIAB\", "
                << "\"TOTAL_NONCURRENT_LIAB\", \"TOTAL_LIABILITIES\", "
                << "\"SHARE_CAPITAL\", \"CAPITAL_RESERVE\", \"OTHER_COMPRE_INCOME\", "
                << "\"SURPLUS_RESERVE\", \"UNASSIGN_RPOFIT\", \"TOTAL_PARENT_EQUITY\", "
                << "\"MINORITY_EQUITY\", \"TOTAL_EQUITY\", \"TOTAL_LIAB_EQUITY\", "
                << "\"MONETARYFUNDS_YOY\", \"TOTAL_ASSETS_YOY\", "
                << "\"TOTAL_LIABILITIES_YOY\", \"TOTAL_EQUITY_YOY\") VALUES ";

            bool first = true;
            for (size_t j = i; j < end; ++j) {
                const auto& rec = records[j];
                if (!first) sql << ", ";
                first = false;

                // 构建一行 VALUES
                sql << "("
                    << sqlQuote(getJsonString(rec, "SECUCODE")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_NAME_ABBR")) << ","
                    << sqlQuote(getJsonString(rec, "ORG_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "ORG_TYPE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_TYPE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_DATE_NAME")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_TYPE_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "NOTICE_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "UPDATE_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "CURRENCY")) << ","

                    << sqlNumeric(rec, "MONETARYFUNDS") << ","
                    << sqlNumeric(rec, "NOTE_ACCOUNTS_RECE") << ","
                    << sqlNumeric(rec, "NOTE_RECE") << ","
                    << sqlNumeric(rec, "ACCOUNTS_RECE") << ","
                    << sqlNumeric(rec, "PREPAYMENT") << ","
                    << sqlNumeric(rec, "OTHER_RECE") << ","
                    << sqlNumeric(rec, "INVENTORY") << ","
                    << sqlNumeric(rec, "OTHER_CURRENT_ASSET") << ","
                    << sqlNumeric(rec, "TOTAL_CURRENT_ASSETS") << ","
                    << sqlNumeric(rec, "LONG_EQUITY_INVEST") << ","
                    << sqlNumeric(rec, "OTHER_EQUITY_INVEST") << ","
                    << sqlNumeric(rec, "OTHER_NONCURRENT_FINASSET") << ","
                    << sqlNumeric(rec, "INVEST_REALESTATE") << ","
                    << sqlNumeric(rec, "FIXED_ASSET") << ","
                    << sqlNumeric(rec, "CIP") << ","
                    << sqlNumeric(rec, "USERIGHT_ASSET") << ","
                    << sqlNumeric(rec, "INTANGIBLE_ASSET") << ","
                    << sqlNumeric(rec, "LONG_PREPAID_EXPENSE") << ","
                    << sqlNumeric(rec, "DEFER_TAX_ASSET") << ","
                    << sqlNumeric(rec, "OTHER_NONCURRENT_ASSET") << ","
                    << sqlNumeric(rec, "TOTAL_NONCURRENT_ASSETS") << ","
                    << sqlNumeric(rec, "TOTAL_ASSETS") << ","

                    << sqlNumeric(rec, "SHORT_LOAN") << ","
                    << sqlNumeric(rec, "NOTE_ACCOUNTS_PAYABLE") << ","
                    << sqlNumeric(rec, "NOTE_PAYABLE") << ","
                    << sqlNumeric(rec, "ACCOUNTS_PAYABLE") << ","
                    << sqlNumeric(rec, "ADVANCE_RECEIVABLES") << ","
                    << sqlNumeric(rec, "CONTRACT_LIAB") << ","
                    << sqlNumeric(rec, "STAFF_SALARY_PAYABLE") << ","
                    << sqlNumeric(rec, "TAX_PAYABLE") << ","
                    << sqlNumeric(rec, "OTHER_PAYABLE") << ","
                    << sqlNumeric(rec, "DIVIDEND_PAYABLE") << ","
                    << sqlNumeric(rec, "NONCURRENT_LIAB_1YEAR") << ","
                    << sqlNumeric(rec, "OTHER_CURRENT_LIAB") << ","
                    << sqlNumeric(rec, "TOTAL_CURRENT_LIAB") << ","

                    << sqlNumeric(rec, "LONG_LOAN") << ","
                    << sqlNumeric(rec, "LEASE_LIAB") << ","
                    << sqlNumeric(rec, "LONG_PAYABLE") << ","
                    << sqlNumeric(rec, "DEFER_TAX_LIAB") << ","
                    << sqlNumeric(rec, "OTHER_NONCURRENT_LIAB") << ","
                    << sqlNumeric(rec, "TOTAL_NONCURRENT_LIAB") << ","
                    << sqlNumeric(rec, "TOTAL_LIABILITIES") << ","

                    << sqlNumeric(rec, "SHARE_CAPITAL") << ","
                    << sqlNumeric(rec, "CAPITAL_RESERVE") << ","
                    << sqlNumeric(rec, "OTHER_COMPRE_INCOME") << ","
                    << sqlNumeric(rec, "SURPLUS_RESERVE") << ","
                    << sqlNumeric(rec, "UNASSIGN_RPOFIT") << ","
                    << sqlNumeric(rec, "TOTAL_PARENT_EQUITY") << ","
                    << sqlNumeric(rec, "MINORITY_EQUITY") << ","
                    << sqlNumeric(rec, "TOTAL_EQUITY") << ","
                    << sqlNumeric(rec, "TOTAL_LIAB_EQUITY") << ","

                    << sqlNumeric(rec, "MONETARYFUNDS_YOY") << ","
                    << sqlNumeric(rec, "TOTAL_ASSETS_YOY") << ","
                    << sqlNumeric(rec, "TOTAL_LIABILITIES_YOY") << ","
                    << sqlNumeric(rec, "TOTAL_EQUITY_YOY")
                    << ")";
            }
            sql << " ON CONFLICT (\"SECUCODE\", \"REPORT_DATE\") DO NOTHING;";
            txn.exec(sql.str());
        }

        txn.commit();
        std::cout << "[PostgresManager] 资产负债表数据插入成功，共 " << total << " 条记录。" << std::endl;
        return true;
    }
    catch (const std::exception& e) {
        std::cerr << "[PostgresManager] 插入资产负债表失败: " << e.what() << std::endl;
        return false;
    }
}

// ---------- InsertIncomeStatement 实现 ----------
bool PostgresManager::InsertIncomeStatement(const nlohmann::json& jsonData) {
    std::lock_guard<std::mutex> lock(m_mutex);
    if (!CheckConnection()) return false;

    if (!jsonData.is_array() && !jsonData.is_object()) {
        std::cerr << "[PostgresManager] 无效的JSON数据，必须是对象或数组。" << std::endl;
        return false;
    }

    try {
        pqxx::work txn(*m_conn);

        nlohmann::json records;
        if (jsonData.is_object()) {
            records = nlohmann::json::array({ jsonData });
        }
        else {
            records = jsonData;
        }

        const size_t BATCH_SIZE = 1000;
        size_t total = records.size();
        for (size_t i = 0; i < total; i += BATCH_SIZE) {
            size_t end = std::min<size_t>(i + BATCH_SIZE, total);
            std::ostringstream sql;
            sql << "INSERT INTO income_statement ("
                << "\"SECUCODE\", \"SECURITY_CODE\", \"SECURITY_NAME_ABBR\", "
                << "\"ORG_CODE\", \"ORG_TYPE\", \"REPORT_DATE\", \"REPORT_TYPE\", "
                << "\"REPORT_DATE_NAME\", \"SECURITY_TYPE_CODE\", \"NOTICE_DATE\", "
                << "\"UPDATE_DATE\", \"CURRENCY\", "
                << "\"TOTAL_OPERATE_INCOME\", \"TOTAL_OPERATE_INCOME_YOY\", "
                << "\"OPERATE_INCOME\", \"OPERATE_INCOME_YOY\", "
                << "\"INTEREST_INCOME\", \"FEE_COMMISSION_INCOME\", "
                << "\"OTHER_BUSINESS_INCOME\", "
                << "\"TOTAL_OPERATE_COST\", \"TOTAL_OPERATE_COST_YOY\", "
                << "\"OPERATE_COST\", \"OPERATE_COST_YOY\", "
                << "\"INTEREST_EXPENSE\", \"FEE_COMMISSION_EXPENSE\", "
                << "\"RESEARCH_EXPENSE\", \"RESEARCH_EXPENSE_YOY\", "
                << "\"OPERATE_TAX_ADD\", \"OPERATE_TAX_ADD_YOY\", "
                << "\"SALE_EXPENSE\", \"SALE_EXPENSE_YOY\", "
                << "\"MANAGE_EXPENSE\", \"MANAGE_EXPENSE_YOY\", "
                << "\"FINANCE_EXPENSE\", \"FINANCE_EXPENSE_YOY\", "
                << "\"FE_INTEREST_EXPENSE\", \"FE_INTEREST_INCOME\", "
                << "\"INVEST_INCOME\", \"INVEST_INCOME_YOY\", "
                << "\"INVEST_JOINT_INCOME\", "
                << "\"ASSET_DISPOSAL_INCOME\", "
                << "\"ASSET_IMPAIRMENT_INCOME\", "
                << "\"CREDIT_IMPAIRMENT_INCOME\", "
                << "\"OTHER_INCOME\", "
                << "\"FAIRVALUE_CHANGE_INCOME\", "
                << "\"OPERATE_PROFIT\", \"OPERATE_PROFIT_YOY\", "
                << "\"NONBUSINESS_INCOME\", \"NONBUSINESS_EXPENSE\", "
                << "\"TOTAL_PROFIT\", \"TOTAL_PROFIT_YOY\", "
                << "\"INCOME_TAX\", "
                << "\"NETPROFIT\", \"NETPROFIT_YOY\", "
                << "\"CONTINUED_NETPROFIT\", "
                << "\"PARENT_NETPROFIT\", "
                << "\"MINORITY_INTEREST\", "
                << "\"DEDUCT_PARENT_NETPROFIT\", "
                << "\"BASIC_EPS\", \"DILUTED_EPS\", "
                << "\"OTHER_COMPRE_INCOME\", "
                << "\"PARENT_OCI\", "
                << "\"ABLE_OCI\", "
                << "\"UNABLE_OCI\", "
                << "\"CONVERT_DIFF\", "
                << "\"TOTAL_COMPRE_INCOME\", "
                << "\"PARENT_TCI\", "
                << "\"MINORITY_TCI\") VALUES ";

            bool first = true;
            for (size_t j = i; j < end; ++j) {
                const auto& rec = records[j];
                if (!first) sql << ", ";
                first = false;

                sql << "("
                    << sqlQuote(getJsonString(rec, "SECUCODE")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_NAME_ABBR")) << ","
                    << sqlQuote(getJsonString(rec, "ORG_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "ORG_TYPE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_TYPE")) << ","
                    << sqlQuote(getJsonString(rec, "REPORT_DATE_NAME")) << ","
                    << sqlQuote(getJsonString(rec, "SECURITY_TYPE_CODE")) << ","
                    << sqlQuote(getJsonString(rec, "NOTICE_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "UPDATE_DATE")) << ","
                    << sqlQuote(getJsonString(rec, "CURRENCY")) << ","

                    << sqlNumeric(rec, "TOTAL_OPERATE_INCOME") << ","
                    << sqlNumeric(rec, "TOTAL_OPERATE_INCOME_YOY") << ","
                    << sqlNumeric(rec, "OPERATE_INCOME") << ","
                    << sqlNumeric(rec, "OPERATE_INCOME_YOY") << ","
                    << sqlNumeric(rec, "INTEREST_INCOME") << ","
                    << sqlNumeric(rec, "FEE_COMMISSION_INCOME") << ","
                    << sqlNumeric(rec, "OTHER_BUSINESS_INCOME") << ","
                    << sqlNumeric(rec, "TOTAL_OPERATE_COST") << ","
                    << sqlNumeric(rec, "TOTAL_OPERATE_COST_YOY") << ","
                    << sqlNumeric(rec, "OPERATE_COST") << ","
                    << sqlNumeric(rec, "OPERATE_COST_YOY") << ","
                    << sqlNumeric(rec, "INTEREST_EXPENSE") << ","
                    << sqlNumeric(rec, "FEE_COMMISSION_EXPENSE") << ","
                    << sqlNumeric(rec, "RESEARCH_EXPENSE") << ","
                    << sqlNumeric(rec, "RESEARCH_EXPENSE_YOY") << ","
                    << sqlNumeric(rec, "OPERATE_TAX_ADD") << ","
                    << sqlNumeric(rec, "OPERATE_TAX_ADD_YOY") << ","
                    << sqlNumeric(rec, "SALE_EXPENSE") << ","
                    << sqlNumeric(rec, "SALE_EXPENSE_YOY") << ","
                    << sqlNumeric(rec, "MANAGE_EXPENSE") << ","
                    << sqlNumeric(rec, "MANAGE_EXPENSE_YOY") << ","
                    << sqlNumeric(rec, "FINANCE_EXPENSE") << ","
                    << sqlNumeric(rec, "FINANCE_EXPENSE_YOY") << ","
                    << sqlNumeric(rec, "FE_INTEREST_EXPENSE") << ","
                    << sqlNumeric(rec, "FE_INTEREST_INCOME") << ","
                    << sqlNumeric(rec, "INVEST_INCOME") << ","
                    << sqlNumeric(rec, "INVEST_INCOME_YOY") << ","
                    << sqlNumeric(rec, "INVEST_JOINT_INCOME") << ","
                    << sqlNumeric(rec, "ASSET_DISPOSAL_INCOME") << ","
                    << sqlNumeric(rec, "ASSET_IMPAIRMENT_INCOME") << ","
                    << sqlNumeric(rec, "CREDIT_IMPAIRMENT_INCOME") << ","
                    << sqlNumeric(rec, "OTHER_INCOME") << ","
                    << sqlNumeric(rec, "FAIRVALUE_CHANGE_INCOME") << ","
                    << sqlNumeric(rec, "OPERATE_PROFIT") << ","
                    << sqlNumeric(rec, "OPERATE_PROFIT_YOY") << ","
                    << sqlNumeric(rec, "NONBUSINESS_INCOME") << ","
                    << sqlNumeric(rec, "NONBUSINESS_EXPENSE") << ","
                    << sqlNumeric(rec, "TOTAL_PROFIT") << ","
                    << sqlNumeric(rec, "TOTAL_PROFIT_YOY") << ","
                    << sqlNumeric(rec, "INCOME_TAX") << ","
                    << sqlNumeric(rec, "NETPROFIT") << ","
                    << sqlNumeric(rec, "NETPROFIT_YOY") << ","
                    << sqlNumeric(rec, "CONTINUED_NETPROFIT") << ","
                    << sqlNumeric(rec, "PARENT_NETPROFIT") << ","
                    << sqlNumeric(rec, "MINORITY_INTEREST") << ","
                    << sqlNumeric(rec, "DEDUCT_PARENT_NETPROFIT") << ","
                    << sqlNumeric(rec, "BASIC_EPS") << ","
                    << sqlNumeric(rec, "DILUTED_EPS") << ","
                    << sqlNumeric(rec, "OTHER_COMPRE_INCOME") << ","
                    << sqlNumeric(rec, "PARENT_OCI") << ","
                    << sqlNumeric(rec, "ABLE_OCI") << ","
                    << sqlNumeric(rec, "UNABLE_OCI") << ","
                    << sqlNumeric(rec, "CONVERT_DIFF") << ","
                    << sqlNumeric(rec, "TOTAL_COMPRE_INCOME") << ","
                    << sqlNumeric(rec, "PARENT_TCI") << ","
                    << sqlNumeric(rec, "MINORITY_TCI")
                    << ")";
            }
            sql << " ON CONFLICT (\"SECUCODE\", \"REPORT_DATE\") DO NOTHING;";
            txn.exec(sql.str());
        }

        txn.commit();
        std::cout << "[PostgresManager] 利润表数据插入成功，共 " << total << " 条记录。" << std::endl;
        return true;
    }
    catch (const std::exception& e) {
        std::cerr << "[PostgresManager] 插入利润表失败: " << e.what() << std::endl;
        return false;
    }
}

bool PostgresManager::InsertCashFlowStatement(const nlohmann::json& jsonData) {
    std::lock_guard<std::mutex> lock(m_mutex);
    if (!CheckConnection()) return false;

    if (!jsonData.is_array() && !jsonData.is_object()) {
        std::cerr << "[PostgresManager] 无效的JSON数据，必须是对象或数组。" << std::endl;
        return false;
    }

    try {
        pqxx::work txn(*m_conn);

        nlohmann::json records;
        if (jsonData.is_object()) {
            records = nlohmann::json::array({ jsonData });
        }
        else {
            records = jsonData;
        }

        // 定义所有列名（与表结构完全一致，不含_YOY字段）
        // 注意：此处列名顺序应与表定义一致，但顺序不影响结果
        std::vector<std::string> columns = {
            "SECUCODE", "SECURITY_CODE", "SECURITY_NAME_ABBR", "ORG_CODE", "ORG_TYPE",
            "REPORT_DATE", "REPORT_TYPE", "REPORT_DATE_NAME", "SECURITY_TYPE_CODE",
            "NOTICE_DATE", "UPDATE_DATE", "CURRENCY",
            // 经营活动现金流
            "SALES_SERVICES", "DEPOSIT_INTERBANK_ADD", "LOAN_PBC_ADD", "OFI_BF_ADD",
            "RECEIVE_ORIGIC_PREMIUM", "RECEIVE_REINSURE_NET", "INSURED_INVEST_ADD",
            "DISPOSAL_TFA_ADD", "RECEIVE_INTEREST_COMMISSION", "BORROW_FUND_ADD",
            "LOAN_ADVANCE_REDUCE", "REPO_BUSINESS_ADD", "RECEIVE_TAX_REFUND",
            "RECEIVE_OTHER_OPERATE", "OPERATE_INFLOW_OTHER", "OPERATE_INFLOW_BALANCE",
            "TOTAL_OPERATE_INFLOW",
            "BUY_SERVICES", "LOAN_ADVANCE_ADD", "PBC_INTERBANK_ADD",
            "PAY_ORIGIC_COMPENSATE", "PAY_INTEREST_COMMISSION", "PAY_POLICY_BONUS",
            "PAY_STAFF_CASH", "PAY_ALL_TAX", "PAY_OTHER_OPERATE",
            "OPERATE_OUTFLOW_OTHER", "OPERATE_OUTFLOW_BALANCE", "TOTAL_OPERATE_OUTFLOW",
            "OPERATE_NETCASH_OTHER", "OPERATE_NETCASH_BALANCE", "NETCASH_OPERATE",
            // 投资活动现金流
            "WITHDRAW_INVEST", "RECEIVE_INVEST_INCOME", "DISPOSAL_LONG_ASSET",
            "DISPOSAL_SUBSIDIARY_OTHER", "REDUCE_PLEDGE_TIMEDEPOSITS", "RECEIVE_OTHER_INVEST",
            "INVEST_INFLOW_OTHER", "INVEST_INFLOW_BALANCE", "TOTAL_INVEST_INFLOW",
            "CONSTRUCT_LONG_ASSET", "INVEST_PAY_CASH", "PLEDGE_LOAN_ADD",
            "OBTAIN_SUBSIDIARY_OTHER", "ADD_PLEDGE_TIMEDEPOSITS", "PAY_OTHER_INVEST",
            "INVEST_OUTFLOW_OTHER", "INVEST_OUTFLOW_BALANCE", "TOTAL_INVEST_OUTFLOW",
            "INVEST_NETCASH_OTHER", "INVEST_NETCASH_BALANCE", "NETCASH_INVEST",
            // 筹资活动现金流
            "ACCEPT_INVEST_CASH", "SUBSIDIARY_ACCEPT_INVEST", "RECEIVE_LOAN_CASH",
            "ISSUE_BOND", "RECEIVE_OTHER_FINANCE", "FINANCE_INFLOW_OTHER",
            "FINANCE_INFLOW_BALANCE", "TOTAL_FINANCE_INFLOW",
            "PAY_DEBT_CASH", "ASSIGN_DIVIDEND_PORFIT", "SUBSIDIARY_PAY_DIVIDEND",
            "BUY_SUBSIDIARY_EQUITY", "PAY_OTHER_FINANCE", "SUBSIDIARY_REDUCE_CASH",
            "FINANCE_OUTFLOW_OTHER", "FINANCE_OUTFLOW_BALANCE", "TOTAL_FINANCE_OUTFLOW",
            "FINANCE_NETCASH_OTHER", "FINANCE_NETCASH_BALANCE", "NETCASH_FINANCE",
            // 汇率变动及净增加额
            "RATE_CHANGE_EFFECT", "CCE_ADD_OTHER", "CCE_ADD_BALANCE", "CCE_ADD",
            "BEGIN_CCE", "END_CCE_OTHER", "END_CCE_BALANCE", "END_CCE",
            // 补充资料
            "NETPROFIT", "ASSET_IMPAIRMENT", "FA_IR_DEPR", "OILGAS_BIOLOGY_DEPR",
            "IR_DEPR", "IA_AMORTIZE", "LPE_AMORTIZE", "DEFER_INCOME_AMORTIZE",
            "PREPAID_EXPENSE_REDUCE", "ACCRUED_EXPENSE_ADD", "DISPOSAL_LONGASSET_LOSS",
            "FA_SCRAP_LOSS", "FAIRVALUE_CHANGE_LOSS", "FINANCE_EXPENSE", "INVEST_LOSS",
            "DEFER_TAX", "DT_ASSET_REDUCE", "DT_LIAB_ADD", "PREDICT_LIAB_ADD",
            "INVENTORY_REDUCE", "OPERATE_RECE_REDUCE", "OPERATE_PAYABLE_ADD", "OTHER",
            "OPERATE_NETCASH_OTHERNOTE", "OPERATE_NETCASH_BALANCENOTE", "NETCASH_OPERATENOTE",
            // 不涉及现金的重大投资和筹资活动
            "DEBT_TRANSFER_CAPITAL", "CONVERT_BOND_1YEAR", "FINLEASE_OBTAIN_FA",
            "UNINVOLVE_INVESTFIN_OTHER",
            // 现金及现金等价物校验
            "END_CASH", "BEGIN_CASH", "END_CASH_EQUIVALENTS", "BEGIN_CASH_EQUIVALENTS",
            "CCE_ADD_OTHERNOTE", "CCE_ADD_BALANCENOTE", "CCE_ADDNOTE",
            // 其他
            "OPINION_TYPE", "OSOPINION_TYPE", "MINORITY_INTEREST", "USERIGHT_ASSET_AMORTIZE"
        };

        // 构建INSERT语句的列部分
        std::ostringstream sqlHead;
        sqlHead << "INSERT INTO cash_flow_statement (";
        for (size_t i = 0; i < columns.size(); ++i) {
            if (i > 0) sqlHead << ", ";
            sqlHead << "\"" << columns[i] << "\"";
        }
        sqlHead << ") VALUES ";

        const size_t BATCH_SIZE = 1000;
        size_t total = records.size();
        for (size_t i = 0; i < total; i += BATCH_SIZE) {
            size_t end = std::min<size_t>(i + BATCH_SIZE, total);
            std::ostringstream sql;
            sql << sqlHead.str();

            bool first = true;
            for (size_t j = i; j < end; ++j) {
                const auto& rec = records[j];
                if (!first) sql << ", ";
                first = false;

                sql << "(";
                for (size_t k = 0; k < columns.size(); ++k) {
                    if (k > 0) sql << ", ";
                    const std::string& col = columns[k];
                    // 从JSON中取值，若不存在则为空字符串
                    std::string val = getJsonString(rec, col);
                    // 使用sqlQuote统一处理（数字会被转为带引号的字符串，但PostgreSQL可以隐式转换）
                    // 对于数值类型，也可以不加引号，但为了安全，加引号也OK
                    sql << sqlQuote(val);
                }
                sql << ")";
            }
            sql << " ON CONFLICT (\"SECUCODE\", \"REPORT_DATE\") DO NOTHING;";
            txn.exec(sql.str());
        }

        txn.commit();
        std::cout << "[PostgresManager] 现金流量表数据插入成功，共 " << total << " 条记录。" << std::endl;
        return true;
    }
    catch (const std::exception& e) {
        std::cerr << "[PostgresManager] 插入现金流量表失败: " << e.what() << std::endl;
        return false;
    }
}