#pragma once

#include <string>
#include <mutex>
#include <sqlite3.h>

class NewsCacheManager
{
public:
	static NewsCacheManager& Instance()
	{
		static NewsCacheManager instance;
		return instance;
	}

	// ½ûÓÃ¿½ØÅcÙxÖµ
	NewsCacheManager(const NewsCacheManager&) = delete;
	NewsCacheManager& operator=(const NewsCacheManager&) = delete;

public:
	bool Initialize(const std::string& dbPath);

	std::string GetLatestArtCode(const std::string& stockCode);
	void UpdateLatestArtCode(const std::string& stockCode, const std::string& artCode);

private:
	NewsCacheManager();
	~NewsCacheManager();

private:
	sqlite3* m_db{ nullptr };
	std::mutex m_mutex;
};