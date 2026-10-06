// MFCApplication1Dlg.cpp: 實現檔案
//
#pragma once

#include "pch.h"
#include "framework.h"
#include "AStockNewsSpider.h"
#include "InitStockListDlg.h"
#include "afxdialogex.h"
#include "Utils.h"
#include <atomic>
#include <random>
#include <memory>

#include "SpiderTask.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define WM_WEBVIEW2_RESPONSE_2 (WM_USER + 101)

const std::string ID_STOCK_LIST_2 = "business-stocklist";

static std::atomic<int> g_requestId_2{ 0 };

struct WebResponseData2
{
	std::string businessId;
	std::string requestId;
	std::string response;
};

static std::string GenerateRequestId2()
{
	int id = ++g_requestId_2;
	return "request-" + std::to_string(id);
}

static int RandomDelay2()
{
	static std::random_device rd;
	static std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dist(1000, 3000);
	return dist(gen);
}

// InitStockListDlg 對話框
IMPLEMENT_DYNAMIC(InitStockListDlg, CDialogEx)
InitStockListDlg::InitStockListDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_INIT_STOCKLIST, pParent)
{
}

void InitStockListDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BUTTON_INIT, m_btnReq);
	DDX_Control(pDX, IDC_STATIC_CON, m_stCon);
}

BEGIN_MESSAGE_MAP(InitStockListDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_SIZE()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_INIT, &InitStockListDlg::OnBnClickedButtonRequest)
	ON_MESSAGE(WM_WEBVIEW2_RESPONSE_2, OnWebView2Response)
	ON_WM_TIMER()
END_MESSAGE_MAP()

// InitStockListDlg 訊息處理程序

BOOL InitStockListDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 初始化本地 SQLite 進度快取
	m_pSnapshotManager = new TaskQueueSnapshotManager();
	m_pSnapshotManager->InitDB("stock_process.db");

	// 獲取控件初始位置（客戶區座標）
	CRect rect;
	m_stCon.GetWindowRect(&rect);
	ScreenToClient(&rect);

	// 獲取初始客戶區大小
	CRect clientRect;
	GetClientRect(&clientRect);

	m_stCon.ModifyStyle(0, WS_CLIPCHILDREN);

	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != nullptr)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 初始化 COM
	CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);

	// 異步創建 WebView2 環境
	HRESULT hr = CreateCoreWebView2EnvironmentWithOptions(
		nullptr, nullptr, nullptr,
		Callback<ICoreWebView2CreateCoreWebView2EnvironmentCompletedHandler>(
			[this](HRESULT result, ICoreWebView2Environment* env) -> HRESULT {
				if (!env) return E_FAIL;
				m_webviewEnv = env;
				HRESULT hr2 = m_webviewEnv->QueryInterface(IID_PPV_ARGS(&m_webviewEnv2));
				if (FAILED(hr2)) {
					TRACE("獲取 ICoreWebView2Environment2 失敗: 0x%08X\n", hr2);
				}

				env->CreateCoreWebView2Controller(
					m_stCon.GetSafeHwnd(),
					Callback<ICoreWebView2CreateCoreWebView2ControllerCompletedHandler>(
						[this](HRESULT result, ICoreWebView2Controller* controller) -> HRESULT {
							if (!controller) return E_FAIL;
							m_webviewController = controller;
							m_webviewController->get_CoreWebView2(&m_webview);
							RECT rc;
							m_stCon.GetClientRect(&rc);
							m_webviewController->put_Bounds(rc);

							RegisterWebViewEvents();
							m_webview->Navigate(L"https://www.eastmoney.com/");
							return S_OK;
						}
					).Get()
				);
				return S_OK;
			}
		).Get()
	);

	if (FAILED(hr)) {
		AfxMessageBox(_T("Failed to create WebView2 environment"));
	}

	return TRUE;
}


void InitStockListDlg::OnBnClickedButtonRequest()
{
	if (!m_webview)
		return;

	StartSpider();
}

void InitStockListDlg::StartSpiderTimer()
{
	if (m_spiderTimer != 0)
	{
		KillTimer(m_spiderTimer);
	}

	int delay = RandomDelay2();

	m_spiderTimer = SetTimer(
		1001,
		delay,
		nullptr
	);

	TRACE("下一次請求延遲 %d ms\n", delay);
}

void InitStockListDlg::OnTimer(UINT_PTR nIDEvent)
{
	if (nIDEvent == 1001)
	{
		KillTimer(m_spiderTimer);
		m_spiderTimer = 0;

		if (!m_taskQueue.empty())
		{
			SpiderTask task = m_taskQueue.front();
			m_taskQueue.pop();

			ExecuteTask(task);
		}

		return;
	}

	CDialogEx::OnTimer(nIDEvent);
}

bool InitStockListDlg::OpenUrl(
	const std::string& businessId,
	const CString& url,
	const SpiderTask& task)
{
	if (url.IsEmpty())
	{
		TRACE("URL為空\n");
		return false;
	}

	if (!m_webview)
		return false;

	std::string requestId = GenerateRequestId2();

	// 保存請求上下文
	m_requestTasks[requestId] = task;

	ComPtr<ICoreWebView2_3> webview3;
	HRESULT hr = m_webview->QueryInterface(IID_PPV_ARGS(&webview3));

	if (FAILED(hr))
	{
		return false;
	}

	ComPtr<ICoreWebView2WebResourceRequest> request;

	hr = m_webviewEnv2->CreateWebResourceRequest(
		url.GetString(),
		L"GET",
		nullptr,
		L"",
		&request
	);

	if (FAILED(hr))
	{
		return false;
	}

	ComPtr<ICoreWebView2HttpRequestHeaders> headers;
	request->get_Headers(&headers);

	if (headers)
	{
		CString cBusiness(businessId.c_str());
		headers->SetHeader(L"X-Business-ID", cBusiness);

		CString cRequest(requestId.c_str());
		headers->SetHeader(L"X-Request-ID", cRequest);
	}

	hr = webview3->NavigateWithWebResourceRequest(request.Get());

	if (FAILED(hr))
	{
		m_requestTasks.erase(requestId);
		return false;
	}

	return true;
}

void InitStockListDlg::StartSpider()
{
	while (!m_taskQueue.empty())
		m_taskQueue.pop();

	// 步驟 1 起始：抓取股票列表第 1 頁
	SpiderTask task;
	task.type = SpiderTask::Type::StockList;
	task.stockPageIndex = 1;

	AddTask(task);
	ExecuteNextTask();
}

void InitStockListDlg::AddTask(const SpiderTask& task)
{
	m_taskQueue.push(task);
}

void InitStockListDlg::ExecuteNextTask()
{
	if (m_taskQueue.empty())
	{
		TRACE("任務隊列為空\n");
		return;
	}

	StartSpiderTimer();
}

void InitStockListDlg::ExecuteTask(const SpiderTask& task)
{
	switch (task.type)
	{
	case SpiderTask::Type::StockList:
		GetStockList(task);
		break;
	default:
		break;
	}
}

void InitStockListDlg::RegisterWebViewEvents()
{
	if (!m_webview) return;

	m_webview->AddWebResourceRequestedFilter(
		L"*eastmoney.com*",
		COREWEBVIEW2_WEB_RESOURCE_CONTEXT_ALL
	);

	ComPtr<ICoreWebView2_2> m_webview2;
	HRESULT hr = m_webview->QueryInterface(IID_PPV_ARGS(&m_webview2));
	if (FAILED(hr) || !m_webview2) {
		TRACE("無法獲取 ICoreWebView2_2 介面\n");
		return;
	}

	EventRegistrationToken responseToken;
	hr = m_webview2->add_WebResourceResponseReceived(
		Callback<ICoreWebView2WebResourceResponseReceivedEventHandler>(
			[this](
				ICoreWebView2* sender,
				ICoreWebView2WebResourceResponseReceivedEventArgs* args) -> HRESULT {
					ComPtr<ICoreWebView2WebResourceRequest> request;
					args->get_Request(&request);
					if (!request) return S_OK;

					ComPtr<ICoreWebView2HttpRequestHeaders> headers;
					request->get_Headers(&headers);
					std::string businessId;
					std::string requestId;
					if (headers)
					{
						LPWSTR id = nullptr;
						headers->GetHeader(L"X-Business-ID", &id);
						if (id) {
							businessId = CW2A(id, CP_UTF8);
							CoTaskMemFree(id);
						}

						LPWSTR rid = nullptr;
						headers->GetHeader(L"X-Request-ID", &rid);
						if (rid) {
							requestId = CW2A(rid, CP_UTF8);
							CoTaskMemFree(rid);
						}
					}

					if (businessId.empty() || requestId.empty())
						return S_OK;

					ComPtr<ICoreWebView2WebResourceResponseView> responseView;
					args->get_Response(&responseView);
					if (!responseView) return S_OK;

					int statusCode = 0;
					responseView->get_StatusCode(&statusCode);
					if (statusCode != 200) return S_OK;

					responseView->GetContent(
						Callback<ICoreWebView2WebResourceResponseViewGetContentCompletedHandler>(
							[this, businessId, requestId](HRESULT result, IStream* contentStream) -> HRESULT {
								if (SUCCEEDED(result) && contentStream) {
									STATSTG stat;
									contentStream->Stat(&stat, STATFLAG_NONAME);
									ULONGLONG size = stat.cbSize.QuadPart;
									if (size > 0) {
										std::vector<BYTE> buffer((size_t)size);
										ULONG bytesRead = 0;
										contentStream->Read(buffer.data(), (ULONG)size, &bytesRead);
										std::string responseData((char*)buffer.data(), bytesRead);

										WebResponseData2* pData = new WebResponseData2();
										pData->businessId = businessId;
										pData->requestId = requestId;
										pData->response = responseData;

										::PostMessage(
											GetSafeHwnd(),
											WM_WEBVIEW2_RESPONSE_2,
											0,
											(LPARAM)pData
										);
									}
								}
								return S_OK;
							}
						).Get()
					);
					return S_OK;
			}
		).Get(),
		&responseToken
	);

	if (FAILED(hr)) {
		TRACE("註冊 WebResourceResponseReceived 失敗: 0x%08X\n", hr);
	}
}

LRESULT InitStockListDlg::OnWebView2Response(WPARAM wParam, LPARAM lParam)
{
	// 使用 unique_ptr 防禦內存洩漏
	std::unique_ptr<WebResponseData2> pData(reinterpret_cast<WebResponseData2*>(lParam));
	if (!pData) return 0;

	auto it = m_requestTasks.find(pData->requestId);
	if (it == m_requestTasks.end()) return 0;

	SpiderTask task = it->second;
	m_requestTasks.erase(it);

	try
	{
		if (pData->businessId == ID_STOCK_LIST_2)
		{
			ProcessStockList(task, pData->response);
		}
	}
	catch (const std::exception& e)
	{
		TRACE("處理 WebView2 響應時發生例外: %s\n", e.what());
		// 發生解析例外時，強制執行下一個任務，防止流轉隊列死鎖
		ExecuteNextTask();
	}

	return 0;
}

bool InitStockListDlg::StockListEmpty(const nlohmann::json& jsonData)
{
	try
	{
		if (!jsonData.is_object() || !jsonData.contains("data") || jsonData["data"].is_null())
			return true;

		const auto& data = jsonData["data"];
		if (!data.is_object() || !data.contains("diff") || !data["diff"].is_array())
			return true;

		return data["diff"].empty();
	}
	catch (const std::exception& e)
	{
		TRACE("StockListEmpty 檢查發生例外: %s\n", e.what());
		return true;
	}
}

// ======================================================================
// 步驟 1: 股票列表處理 (逐頁獲取)
// ======================================================================

void InitStockListDlg::GetStockList(const SpiderTask& task)
{
	CString strUrl;
	strUrl.Format(
		L"https://push2.eastmoney.com/weblogin/api/qt/clist/get?"
		L"np=1&fltt=1&invt=2"
		L"&fs=m:0+t:6+f:!2,"
		L"m:0+t:80+f:!2,"
		L"m:1+t:2+f:!2,"
		L"m:1+t:23+f:!2,"
		L"m:0+t:81+s:262144+f:!2"
		L"&fields=f12,f13,f14"
		L"&fid=f3"
		L"&pn=%d"
		L"&pz=20"
		L"&po=1"
		L"&dect=1"
		L"&ut=fa5fd1943c7b386f172d6893dbfba10b"
		L"&_=%ld",
		task.stockPageIndex,
		Utils::GetCurrentTimeMillis()
	);

	OpenUrl(ID_STOCK_LIST_2, strUrl, task);
}

void InitStockListDlg::ProcessStockList(
	const SpiderTask& task,
	const std::string& responseData)
{
	try
	{
		// 1. 解析 JSON 語法
		auto jsonData = nlohmann::json::parse(responseData);

		// 2. 檢查資料是否為空或無效
		if (StockListEmpty(jsonData))
		{
			TRACE("股票列表為空或結構無效，初始化列表快照完成\n");
			return;
		}

		SpiderTask nextTask = task;
		nextTask.currentStockList.clear();

		// 3. 安全讀取股票陣列
		const auto& stocks = jsonData["data"]["diff"];
		const std::list<SpiderTask> stockList = [&stocks]() {
			std::list<SpiderTask> list;
			if (stocks.is_array()) {
				for (const auto& item : stocks) {
					SpiderTask task;
					// f12: 股票代碼, f14: 股票名稱 (依據東財 API 字段)
					task.stockCode = item.value("f12", "");
					task.stockName = item.value("f14", "");

					if (!task.stockCode.empty()) {
						list.push_back(task);
					}
				}
			}
			return list;
			}();
		m_pSnapshotManager->CreateSnapshot(stockList);

		nextTask.stockPageIndex++;
		AddTask(nextTask);
	}
	catch (const nlohmann::json::exception& e)
	{
		TRACE("ProcessStockList JSON 解析異常: %s\n", e.what());
	}
	catch (const std::exception& e)
	{
		TRACE("ProcessStockList 未知例外: %s\n", e.what());
	}

	// 保障機制：無論成功與否，確保執行下一個任務
	ExecuteNextTask();
}
