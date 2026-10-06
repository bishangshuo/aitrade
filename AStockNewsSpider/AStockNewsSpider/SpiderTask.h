#pragma once
#include <string>
#include <vector>

struct StockItem
{
	std::string stockCode;
	std::string stockName;
};

struct SpiderTask
{
	enum class Type
	{
		StockList,          // 股票列表
		StockNewsList,      // 股票公告列表
		NewsDetail          // 公告詳情
	};

	Type type{ Type::StockList };

	// ==========================================
	// 狀態控管欄位（實現層級流轉）
	// ==========================================
	// 股票列表層級
	int stockPageIndex{ 1 };                    // 股票列表頁碼
	std::vector<StockItem> currentStockList;   // 當前頁面的所有股票
	size_t currentStockIndex{ 0 };             // 當前處理到第幾隻股票

	// 公告列表層級
	int newsPageIndex{ 1 };                     // 當前股票的公告頁碼

	// ==========================================
	// 單個業務上下文欄位
	// ==========================================
	// 股票代碼
	std::string stockCode;

	// 股票名稱
	std::string stockName;

	// 市場類型
	std::string marketCode;

	// 公告類型
	std::string annType;

	// 資訊類型
	std::string sourceType;

	// 公告編號
	std::string artCode;

	int status;
	int lastPage;

	SpiderTask()
	{
	}

	SpiderTask(Type t)
		: type(t)
	{
	}
};