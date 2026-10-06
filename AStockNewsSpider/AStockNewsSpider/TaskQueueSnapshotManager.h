#pragma once
#include <sqlite3.h>
#include <string>
#include <list>
#include <iostream>

#include "SpiderTask.h"

class TaskQueueSnapshotManager {
public:
    TaskQueueSnapshotManager();
    ~TaskQueueSnapshotManager();

    // 初始化 SQLite 數據庫與表結構
    bool InitDB(const std::string& dbPath = "spider_progress.db");

    // 檢查 SQLite 中是否已存在任務快照
    bool HasSnapshot();

    // 將 API 獲取到的全量股票列表寫入 SQLite（生成任務快照）
    bool CreateSnapshot(const std::list<SpiderTask>& stockList);

    // 獲取下一個未完成的股票任務 (status != 2)
    bool GetNextUnfinishedTask(SpiderTask& outTask);

    // 更新當前股票的頁碼進度
    bool UpdateTaskProgress(const std::string& stockCode, int currentPage);

    // 標記該股票全量爬取完成 (status = 2)
    bool MarkTaskCompleted(const std::string& stockCode);

    // 獲取總體進度統計信息
    void GetProgressStats(int& total, int& completed, int& inProgress);

private:
    sqlite3* m_db;
};
