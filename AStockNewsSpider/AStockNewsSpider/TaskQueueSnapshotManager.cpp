#include "pch.h"
#include "TaskQueueSnapshotManager.h"

TaskQueueSnapshotManager::TaskQueueSnapshotManager() : m_db(nullptr) {}

TaskQueueSnapshotManager::~TaskQueueSnapshotManager() {
    if (m_db) {
        sqlite3_close(m_db);
        m_db = nullptr;
    }
}

bool TaskQueueSnapshotManager::InitDB(const std::string& dbPath) {
    if (sqlite3_open(dbPath.c_str(), &m_db) != SQLITE_OK) {
        std::cerr << "[DB Error] 無法打開/創建 SQLite 數據庫: " << sqlite3_errmsg(m_db) << std::endl;
        return false;
    }

    // 創建股票任務快照表
    const char* sqlCreateTable =
        "CREATE TABLE IF NOT EXISTS stock_task_queue ("
        "stock_code TEXT PRIMARY KEY, "
        "stock_name TEXT, "
        "status INT DEFAULT 0, "          // 0: 未開始, 1: 進行中, 2: 已完成
        "last_page INT DEFAULT 1, "       // 斷點續傳核心頁碼
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    char* errMsg = nullptr;
    if (sqlite3_exec(m_db, sqlCreateTable, nullptr, nullptr, &errMsg) != SQLITE_OK) {
        std::cerr << "[DB Error] 創建表失敗: " << (errMsg ? errMsg : "") << std::endl;
        if (errMsg) sqlite3_free(errMsg);
        return false;
    }
    return true;
}

bool TaskQueueSnapshotManager::HasSnapshot() {
    const char* sql = "SELECT COUNT(*) FROM stock_task_queue;";
    sqlite3_stmt* stmt = nullptr;
    int count = 0;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            count = sqlite3_column_int(stmt, 0);
        }
    }
    sqlite3_finalize(stmt);
    return count > 0;
}

bool TaskQueueSnapshotManager::CreateSnapshot(const std::list<SpiderTask>& stockList) {
    if (stockList.empty()) return false;

    // 使用事務 (Transaction) 進行批量插入，速度提升 100 倍以上
    sqlite3_exec(m_db, "BEGIN TRANSACTION;", nullptr, nullptr, nullptr);

    const char* sql = "INSERT OR IGNORE INTO stock_task_queue (stock_code, stock_name, status, last_page) VALUES (?, ?, 0, 1);";
    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
        sqlite3_exec(m_db, "ROLLBACK;", nullptr, nullptr, nullptr);
        return false;
    }

    for (const auto& stock : stockList) {
        sqlite3_bind_text(stmt, 1, stock.stockCode.c_str(), -1, SQLITE_STATIC);
        sqlite3_bind_text(stmt, 2, stock.stockName.c_str(), -1, SQLITE_STATIC);
        sqlite3_step(stmt);
        sqlite3_reset(stmt);
    }

    sqlite3_finalize(stmt);
    sqlite3_exec(m_db, "COMMIT;", nullptr, nullptr, nullptr);
    std::cout << "[Snapshot] 成功生成任務快照！共寫入 " << stockList.size() << " 隻股票。" << std::endl;
    return true;
}

bool TaskQueueSnapshotManager::GetNextUnfinishedTask(SpiderTask& outTask) {
    // 優先讀取「進行中 (status=1)」的股票，如果沒有，再讀取「未開始 (status=0)」的股票
    const char* sql = "SELECT stock_code, stock_name, status, last_page "
        "FROM stock_task_queue "
        "WHERE status != 2 "
        "ORDER BY status DESC, created_at ASC LIMIT 1;";

    sqlite3_stmt* stmt = nullptr;
    bool found = false;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            outTask.stockCode = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
            outTask.stockName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
            outTask.status = sqlite3_column_int(stmt, 2);
            outTask.lastPage = sqlite3_column_int(stmt, 3);
            found = true;
        }
    }
    sqlite3_finalize(stmt);
    return found;
}

bool TaskQueueSnapshotManager::UpdateTaskProgress(const std::string& stockCode, int currentPage) {
    const char* sql = "UPDATE stock_task_queue SET status = 1, last_page = ?, updated_at = CURRENT_TIMESTAMP WHERE stock_code = ?;";
    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, currentPage);
        sqlite3_bind_text(stmt, 2, stockCode.c_str(), -1, SQLITE_STATIC);
        sqlite3_step(stmt);
        sqlite3_finalize(stmt);
        return true;
    }
    return false;
}

bool TaskQueueSnapshotManager::MarkTaskCompleted(const std::string& stockCode) {
    const char* sql = "UPDATE stock_task_queue SET status = 2, updated_at = CURRENT_TIMESTAMP WHERE stock_code = ?;";
    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, stockCode.c_str(), -1, SQLITE_STATIC);
        sqlite3_step(stmt);
        sqlite3_finalize(stmt);
        return true;
    }
    return false;
}

void TaskQueueSnapshotManager::GetProgressStats(int& total, int& completed, int& inProgress) {
    total = completed = inProgress = 0;
    const char* sql = "SELECT status, COUNT(*) FROM stock_task_queue GROUP BY status;";
    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(m_db, sql, -1, &stmt, nullptr) == SQLITE_OK) {
        while (sqlite3_step(stmt) == SQLITE_ROW) {
            int st = sqlite3_column_int(stmt, 0);
            int cnt = sqlite3_column_int(stmt, 1);
            total += cnt;
            if (st == 2) completed = cnt;
            else if (st == 1) inProgress = cnt;
        }
    }
    sqlite3_finalize(stmt);
}
