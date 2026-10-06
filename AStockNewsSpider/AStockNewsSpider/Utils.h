#pragma once

#include <string>
#include <sstream>
#include <ctime>
#include <chrono>
#include <random>
using namespace std;

class Utils
{
public:
	// 获取最近N个季度末日期字符串
	static std::string GetLastQuarterEndList(int count = 13);

	//获取当前时间戳，单位毫秒
	static long long GetCurrentTimeMillis();

	//生产调用版本号
	static std::string generateNumericVersion(size_t length);

	/**
	* 根据股票代码（f12）和市场字段（f13）判断所属交易所，输出 "SH"（上海）、"SZ"（深圳）或 "BJ"（北京）。
	*/
	static std::string getExchange(const std::string& f12, int f13);

	/**
	 * 判断股票是否为普通股（非ST、非新股、非退市整理期）
	 * @param f14  股票简称（例如 "五洲医疗"、"N长鑫"、"ST五洲"）
	 * @param f152 状态标志（2=正常，其他可能表示异常）
	 * @return true 表示是普通股，false 表示不是
	 */
	static bool isCommonStock(const std::string& f14, int f152);
};

