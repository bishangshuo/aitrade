#pragma once
#include "afxdialogex.h"


#include <WebView2.h>

#include <wrl/client.h>
#include <wrl/event.h>

#include <atlconv.h>

#include <nlohmann/json.hpp>

#include <queue>
#include <unordered_map>
#include <string>

#include "TaskQueueSnapshotManager.h"

using Microsoft::WRL::ComPtr;
using Microsoft::WRL::Callback;

#include "SpiderTask.h"

class InitStockListDlg : public CDialogEx
{
	DECLARE_DYNAMIC(InitStockListDlg)

public:
	InitStockListDlg(CWnd* pParent = nullptr);   // 标准构造函数

	// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DIALOG_INIT_STOCKLIST };
#endif

protected:
	virtual BOOL OnInitDialog();

	afx_msg LRESULT OnWebView2Response(
		WPARAM wParam,
		LPARAM lParam);

	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	afx_msg void OnTimer(
		UINT_PTR nIDEvent);

	afx_msg void OnBnClickedButtonRequest();

	DECLARE_MESSAGE_MAP()

private:
	//==============================
	// WebView2
	//==============================

	ComPtr<ICoreWebView2Controller>
		m_webviewController;

	ComPtr<ICoreWebView2>
		m_webview;

	ComPtr<ICoreWebView2Environment>
		m_webviewEnv;

	ComPtr<ICoreWebView2Environment2>
		m_webviewEnv2;

private:

	//==============================
	// 定時器
	//==============================

	UINT_PTR m_spiderTimer{ 0 };
	void StartSpiderTimer();

private:

	//==============================
	// 任務隊列
	//==============================

	std::queue<SpiderTask>
		m_taskQueue;

	std::unordered_map<
		std::string,
		SpiderTask
	>
		m_requestTasks;

private:

	//==============================
	// WebView2
	//==============================

	void RegisterWebViewEvents();

	bool OpenUrl(
		const std::string& businessId,
		const CString& url,
		const SpiderTask& task);

private:

	//==============================
	// Spider核心
	//==============================

	void StartSpider();

	void AddTask(
		const SpiderTask& task);

	void ExecuteNextTask();

	void ExecuteTask(
		const SpiderTask& task);

private:

	//==============================
	// 股票列表
	//==============================

	void GetStockList(
		const SpiderTask& task);

	void ProcessStockList(
		const SpiderTask& task,
		const std::string& responseData);

	bool StockListEmpty(
		const nlohmann::json& jsonData);


	CButton m_btnReq;

	CStatic m_stCon;

	TaskQueueSnapshotManager* m_pSnapshotManager;
};