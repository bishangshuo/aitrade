#include "pch.h"
#include "Utils.h"

std::string Utils::GetLastQuarterEndList(int count)
{
	time_t now = time(nullptr);
	tm localTime{};

#ifdef _WIN32
	localtime_s(&localTime, &now);
#else
	localtime_r(&now, &localTime);
#endif

	int year = localTime.tm_year + 1900;
	int month = localTime.tm_mon + 1;
	int day = localTime.tm_mday;

	// 根据当前日期确定最近已结束季度
	int quarter;

	if (month > 3 || (month == 3 && day >= 31))
	{
		quarter = 1;
	}

	if (month > 6 || (month == 6 && day >= 30))
	{
		quarter = 2;
	}

	if (month > 9 || (month == 9 && day >= 30))
	{
		quarter = 3;
	}

	if (month > 12 || (month == 12 && day >= 31))
	{
		quarter = 4;
	}

	// 如果当前日期处于季度内但未到季度末
	if ((month == 3 && day < 31) ||
		(month == 6 && day < 30) ||
		(month == 9 && day < 30) ||
		(month == 12 && day < 31))
	{
		quarter--;

		if (quarter == 0)
		{
			quarter = 4;
			year--;
		}
	}

	std::ostringstream oss;

	for (int i = 0; i < count; i++)
	{
		int qMonth;
		int qDay;

		switch (quarter)
		{
		case 1:
			qMonth = 3;
			qDay = 31;
			break;

		case 2:
			qMonth = 6;
			qDay = 30;
			break;

		case 3:
			qMonth = 9;
			qDay = 30;
			break;

		default:
			qMonth = 12;
			qDay = 31;
			break;
		}

		if (i > 0)
			oss << ",";

		oss << "'"
			<< year
			<< "-"
			<< (qMonth < 10 ? "0" : "")
			<< qMonth
			<< "-"
			<< (qDay < 10 ? "0" : "")
			<< qDay
			<< "'";

		// 向前推一个季度
		quarter--;

		if (quarter == 0)
		{
			quarter = 4;
			year--;
		}
	}

	return oss.str();
}

long long Utils::GetCurrentTimeMillis()
{
	auto now = std::chrono::system_clock::now();
	auto timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
		now.time_since_epoch()
	).count();
	return timestamp;
}

std::string Utils::generateNumericVersion(size_t length)
{
	// 使用线程安全的随机数引擎，并用随机设备初始化种子
	thread_local std::mt19937_64 rng(std::random_device{}());
	// 定义数字字符的范围 '0' 到 '9'
	std::uniform_int_distribution<int> dist('0', '9');

	std::string result;
	result.reserve(length); // 预分配内存，避免多次扩容

	for (size_t i = 0; i < length; ++i) {
		result += static_cast<char>(dist(rng));
	}
	return result;
}

std::string Utils::getExchange(const std::string& f12, int f13) {
	// 1. 根据 f13 区分上海市场（f13 == 1）
	if (f13 == 1) {
		return "SH";
	}

	// 2. f13 == 0 时，可能属于深圳或北京，通过代码前缀进一步区分
	if (f13 == 0) {
		// 北交所股票代码前缀：早期（83、87、88）和新代码（920）
		if (f12.compare(0, 2, "83") == 0 ||
			f12.compare(0, 2, "87") == 0 ||
			f12.compare(0, 2, "88") == 0 ||
			f12.compare(0, 3, "920") == 0) {
			return "BJ";
		}
		// 其余（000、001、002、003、300、301 等）均为深圳
		return "SZ";
	}

	// 3. 其他情况（例如 f13 非 0 或 1）返回未知
	return "UNKNOWN";
}

bool Utils::isCommonStock(const std::string& f14, int f152) {
	// 先检查名称前缀（大小写敏感，但API返回的通常为大写）
	if (f14.size() >= 2 && f14.substr(0, 2) == "ST") {
		return false;          // ST股
	}
	if (f14.size() >= 3 && f14.substr(0, 3) == "*ST") {
		return false;          // *ST股
	}
	if (f14.size() >= 1 && f14.substr(0, 1) == "N") {
		return false;          // 新股上市首日（科创板/主板）
	}
	if (f14.size() >= 1 && f14.substr(0, 1) == "C") {
		return false;          // 科创板/创业板新股第2~5日
	}
	if (f14.size() >= 1 && f14.substr(0, 1) == "退") {
		return false;          // 退市整理期股票
	}

	// 辅助检查：若f152不为2，通常表示异常（停牌、退市等），但部分新股也为2，因此仅作参考
	// 若您确定所有异常状态的f152 != 2，可取消注释：
	// if (f152 != 2) return false;

	// 其余情况视为普通股
	return true;
}