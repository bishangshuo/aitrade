#pragma once

#include <WebView2.h>

#include <wrl/client.h>
#include <wrl/event.h>

#include <atlconv.h>

#include <nlohmann/json.hpp>

#include <queue>
#include <unordered_map>
#include <string>

using Microsoft::WRL::ComPtr;
using Microsoft::WRL::Callback;

#include "SpiderTask.h"

class AStockNewsSpiderDlg : public CDialogEx
{
public:

	AStockNewsSpiderDlg(
		CWnd* pParent = nullptr);

#ifdef AFX_DESIGN_TIME

	enum
	{
		IDD = IDD_MFCAPPLICATION1_DIALOG
	};

#endif

protected:

	virtual void DoDataExchange(
		CDataExchange* pDX);

protected:

	HICON m_hIcon;

	virtual BOOL OnInitDialog();

	afx_msg void OnSysCommand(
		UINT nID,
		LPARAM lParam);

	afx_msg void OnPaint();

	afx_msg HCURSOR OnQueryDragIcon();

	afx_msg void OnSize(
		UINT nType,
		int cx,
		int cy);

	afx_msg LRESULT OnWebView2Response(
		WPARAM wParam,
		LPARAM lParam);

	afx_msg void OnTimer(
		UINT_PTR nIDEvent);

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
	// UI佈局
	//==============================

	CRect m_rectConInit;

	int m_nLeft;

	int m_nTop;

	int m_nRightMargin;

	int m_nBottomMargin;

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

	// 流轉輔助：推進至下一個股票或下一頁股票
	void MoveNextStock(SpiderTask task);

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

private:

	//==============================
	// 新聞列表
	//==============================

	void GetStockNewsList(
		const SpiderTask& task);

	void ProcessStockNewsList(
		const SpiderTask& task,
		const std::string& responseData);

	bool NewsListEmpty(
		const nlohmann::json& jsonData);

private:

	//==============================
	// 新聞詳情
	//==============================

	void GetStockNewsDetail(
		const SpiderTask& task);

	void ProcessStockNewsDetail(
		const SpiderTask& task,
		const std::string& responseData);

public:

	CButton m_btnReq;

	CStatic m_stCon;

	afx_msg void OnBnClickedButtonRequest();
	afx_msg void OnBnClickedButtonInitSnapshot();
	afx_msg void OnBnClickedButtonInitNews();
};