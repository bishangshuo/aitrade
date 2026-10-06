// MFCApplication1Dlg.cpp: 實現檔案
//

#include "pch.h"
#include "framework.h"
#include "AStockNewsSpider.h"
#include "AStockNewsSpiderDlg.h"
#include "afxdialogex.h"
#include "Utils.h"
#include <atomic>
#include <random>
#include <memory>
#include "PostgresManager.h"
#include "NewsCacheManager.h"
#include "InitStockListDlg.h"
#include "InitStockNewsDlg.h"

#include "SpiderTask.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define WM_WEBVIEW2_RESPONSE (WM_USER + 100)

const std::string ID_STOCK_LIST = "business-stocklist";
const std::string ID_NEWS_LIST = "business-newslist";
const std::string ID_NEWS_DETAIL = "business-newsdetail";

static std::atomic<int> g_requestId{ 0 };

struct WebResponseData
{
	std::string businessId;
	std::string requestId;
	std::string response;
};

static std::string GenerateRequestId()
{
	int id = ++g_requestId;
	return "request-" + std::to_string(id);
}

static int RandomDelay()
{
	static std::random_device rd;
	static std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dist(1000, 2000);
	return dist(gen);
}

// AStockNewsSpiderDlg 對話框

AStockNewsSpiderDlg::AStockNewsSpiderDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_MFCAPPLICATION1_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void AStockNewsSpiderDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BUTTON_REQUEST, m_btnReq);
	DDX_Control(pDX, IDC_STATIC_CON, m_stCon);
}

BEGIN_MESSAGE_MAP(AStockNewsSpiderDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_SIZE()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_REQUEST, &AStockNewsSpiderDlg::OnBnClickedButtonRequest)
	ON_MESSAGE(WM_WEBVIEW2_RESPONSE, OnWebView2Response)
	ON_WM_TIMER()
	ON_BN_CLICKED(IDC_BUTTON_INIT_SNAPSHOT, &AStockNewsSpiderDlg::OnBnClickedButtonInitSnapshot)
	ON_BN_CLICKED(IDC_BUTTON_INIT_NEWS, &AStockNewsSpiderDlg::OnBnClickedButtonInitNews)
END_MESSAGE_MAP()

// AStockNewsSpiderDlg 訊息處理程序

BOOL AStockNewsSpiderDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	PostgresManager::Instance().Initialize("host=192.168.0.60 port=5432 dbname=kline_db user=postgres password=123456");

	// 初始化本地 SQLite 進度快取
	NewsCacheManager::Instance().Initialize("stock_process.db");

	// 獲取控件初始位置（客戶區座標）
	CRect rect;
	m_stCon.GetWindowRect(&rect);
	ScreenToClient(&rect);
	m_rectConInit = rect;

	m_nLeft = rect.left;
	m_nTop = rect.top;

	// 獲取初始客戶區大小
	CRect clientRect;
	GetClientRect(&clientRect);
	m_nRightMargin = clientRect.right - rect.right;
	m_nBottomMargin = clientRect.bottom - rect.bottom;

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

	SetIcon(m_hIcon, TRUE);
	SetIcon(m_hIcon, FALSE);

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

void AStockNewsSpiderDlg::OnBnClickedButtonInitSnapshot()
{
	InitStockListDlg dlg(this);
	dlg.DoModal();
}

void AStockNewsSpiderDlg::OnBnClickedButtonInitNews()
{
	InitStockNewsDlg dlg(this);
	dlg.DoModal();
}


void AStockNewsSpiderDlg::OnSize(UINT nType, int cx, int cy)
{
	CDialogEx::OnSize(nType, cx, cy);

	if (m_webviewController && m_stCon.GetSafeHwnd())
	{
		CRect clientRect;
		GetClientRect(&clientRect);

		CRect rectControl;
		rectControl.left = m_nLeft;
		rectControl.top = m_nTop;
		rectControl.right = clientRect.right - m_nRightMargin;
		rectControl.bottom = clientRect.bottom - m_nBottomMargin;

		if (rectControl.Width() < 100) rectControl.right = rectControl.left + 100;
		if (rectControl.Height() < 100) rectControl.bottom = rectControl.top + 100;

		m_stCon.MoveWindow(&rectControl);

		CRect rectConClient;
		m_stCon.GetClientRect(&rectConClient);
		m_webviewController->put_Bounds(rectConClient);
	}
}

void AStockNewsSpiderDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this);

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

HCURSOR AStockNewsSpiderDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void AStockNewsSpiderDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

void AStockNewsSpiderDlg::OnBnClickedButtonRequest()
{
	if (!m_webview)
		return;

	StartSpider();
}

void AStockNewsSpiderDlg::StartSpiderTimer()
{
	if (m_spiderTimer != 0)
	{
		KillTimer(m_spiderTimer);
	}

	int delay = RandomDelay();

	m_spiderTimer = SetTimer(
		1001,
		delay,
		nullptr
	);

	TRACE("下一次請求延遲 %d ms\n", delay);
}

void AStockNewsSpiderDlg::OnTimer(UINT_PTR nIDEvent)
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

bool AStockNewsSpiderDlg::OpenUrl(
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

	std::string requestId = GenerateRequestId();

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

void AStockNewsSpiderDlg::StartSpider()
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

void AStockNewsSpiderDlg::AddTask(const SpiderTask& task)
{
	m_taskQueue.push(task);
}

void AStockNewsSpiderDlg::ExecuteNextTask()
{
	if (m_taskQueue.empty())
	{
		TRACE("任務隊列為空\n");
		return;
	}

	StartSpiderTimer();
}

void AStockNewsSpiderDlg::ExecuteTask(const SpiderTask& task)
{
	switch (task.type)
	{
	case SpiderTask::Type::StockList:
		GetStockList(task);
		break;

	case SpiderTask::Type::StockNewsList:
		GetStockNewsList(task);
		break;

	case SpiderTask::Type::NewsDetail:
		GetStockNewsDetail(task);
		break;
	}
}

void AStockNewsSpiderDlg::RegisterWebViewEvents()
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

										WebResponseData* pData = new WebResponseData();
										pData->businessId = businessId;
										pData->requestId = requestId;
										pData->response = responseData;

										::PostMessage(
											GetSafeHwnd(),
											WM_WEBVIEW2_RESPONSE,
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

LRESULT AStockNewsSpiderDlg::OnWebView2Response(WPARAM wParam, LPARAM lParam)
{
	// 使用 unique_ptr 防禦內存洩漏
	std::unique_ptr<WebResponseData> pData(reinterpret_cast<WebResponseData*>(lParam));
	if (!pData) return 0;

	auto it = m_requestTasks.find(pData->requestId);
	if (it == m_requestTasks.end()) return 0;

	SpiderTask task = it->second;
	m_requestTasks.erase(it);

	try
	{
		if (pData->businessId == ID_STOCK_LIST)
		{
			ProcessStockList(task, pData->response);
		}
		else if (pData->businessId == ID_NEWS_LIST)
		{
			ProcessStockNewsList(task, pData->response);
		}
		else if (pData->businessId == ID_NEWS_DETAIL)
		{
			ProcessStockNewsDetail(task, pData->response);
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

bool AStockNewsSpiderDlg::StockListEmpty(const nlohmann::json& jsonData)
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

bool AStockNewsSpiderDlg::NewsListEmpty(const nlohmann::json& jsonData)
{
	try
	{
		if (!jsonData.is_object() || !jsonData.contains("data") || jsonData["data"].is_null())
			return true;

		const auto& data = jsonData["data"];
		if (!data.is_object() || !data.contains("list") || !data["list"].is_array())
			return true;

		return data["list"].empty();
	}
	catch (const std::exception& e)
	{
		TRACE("NewsListEmpty 檢查發生例外: %s\n", e.what());
		return true;
	}
}

// ======================================================================
// 步驟 1: 股票列表處理 (逐頁獲取)
// ======================================================================

void AStockNewsSpiderDlg::GetStockList(const SpiderTask& task)
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

	OpenUrl(ID_STOCK_LIST, strUrl, task);
}

void AStockNewsSpiderDlg::ProcessStockList(
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
			TRACE("股票列表為空或結構無效，重置回第 1 頁\n");
			SpiderTask restartTask;
			restartTask.type = SpiderTask::Type::StockList;
			restartTask.stockPageIndex = 1;

			AddTask(restartTask);
			ExecuteNextTask();
			return;
		}

		SpiderTask nextTask = task;
		nextTask.currentStockList.clear();

		// 3. 安全讀取股票陣列
		const auto& stocks = jsonData["data"]["diff"];
		for (const auto& stock : stocks)
		{
			if (!stock.is_object()) continue;

			StockItem item;
			item.stockCode = stock.value("f12", "");
			item.stockName = stock.value("f14", "");

			if (!item.stockCode.empty())
			{
				nextTask.currentStockList.push_back(item);
			}
		}

		// 4. 推進隊列
		if (!nextTask.currentStockList.empty())
		{
			nextTask.type = SpiderTask::Type::StockNewsList;
			nextTask.currentStockIndex = 0;
			nextTask.stockCode = nextTask.currentStockList[0].stockCode;
			nextTask.stockName = nextTask.currentStockList[0].stockName;
			nextTask.newsPageIndex = 1;

			AddTask(nextTask);
		}
		else
		{
			// 無有效股票，嘗試下一頁
			nextTask.type = SpiderTask::Type::StockList;
			nextTask.stockPageIndex++;
			AddTask(nextTask);
		}
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

// ======================================================================
// 步驟 2 輔助流轉：當前股票公告處理完畢，流轉至下一個股票/下頁股票
// ======================================================================

void AStockNewsSpiderDlg::MoveNextStock(SpiderTask task)
{
	task.currentStockIndex++;

	// 【需求 2】：當前股票列表頁面中，還有未處理的股票
	if (task.currentStockIndex < task.currentStockList.size())
	{
		task.type = SpiderTask::Type::StockNewsList;
		task.stockCode = task.currentStockList[task.currentStockIndex].stockCode;
		task.stockName = task.currentStockList[task.currentStockIndex].stockName;
		task.newsPageIndex = 1; // 重置公告頁碼為 1

		AddTask(task);
	}
	// 【需求 1 下半句】：等 2 處理完成（當前頁股票全爬完）後，獲取下一頁股票列表
	else
	{
		task.type = SpiderTask::Type::StockList;
		task.stockPageIndex++;
		task.currentStockList.clear();
		task.currentStockIndex = 0;

		AddTask(task);
	}

	ExecuteNextTask();
}

// ======================================================================
// 步驟 3: 公告列表處理 (逐頁獲取當前股票公告)
// ======================================================================

void AStockNewsSpiderDlg::GetStockNewsList(const SpiderTask& task)
{
	CString stockCode(task.stockCode.c_str());
	CString url;

	url.Format(
		L"https://np-anotice-stock.eastmoney.com/api/security/ann?"
		L"sr=-1"
		L"&page_size=50"
		L"&page_index=%d"
		L"&ann_type=A"
		L"&client_source=web"
		L"&stock_list=%s"
		L"&f_node=0"
		L"&s_node=0",
		task.newsPageIndex,
		stockCode
	);

	OpenUrl(ID_NEWS_LIST, url, task);
}

void AStockNewsSpiderDlg::ProcessStockNewsList(
	const SpiderTask& task,
	const std::string& responseData)
{
	bool hitOldNews = false;
	std::vector<SpiderTask> detailTasks;

	try
	{
		auto jsonData = nlohmann::json::parse(responseData);

		// 如果公告列表為空，直接切換到下一隻股票
		if (NewsListEmpty(jsonData))
		{
			TRACE("股票 [%s] 公告列表為空，切換至下一隻股票\n", task.stockCode.c_str());
			MoveNextStock(task);
			return;
		}

		const auto& list = jsonData["data"]["list"];
		std::string latestArtCode = NewsCacheManager::Instance().GetLatestArtCode(task.stockCode);

		for (const auto& news : list)
		{
			if (!news.is_object()) continue;

			std::string artCode = news.value("art_code", "");
			if (artCode.empty()) continue;

			// 增量過濾：碰到本地已記錄的最新 artCode 則停止增量
			if (!latestArtCode.empty() && artCode == latestArtCode)
			{
				hitOldNews = true;
				break;
			}

			// 檢查 codes 陣列結構
			std::string annType = "";
			std::string marketCode = "";

			if (news.contains("codes") && news["codes"].is_array() && !news["codes"].empty())
			{
				const auto& codeInfo = news["codes"][0];
				if (codeInfo.is_object())
				{
					annType = codeInfo.value("ann_type", "");
					marketCode = codeInfo.value("market_code", "");
				}
			}

			SpiderTask detail = task;
			detail.type = SpiderTask::Type::NewsDetail;
			detail.artCode = artCode;
			detail.annType = annType;
			detail.marketCode = marketCode;
			detail.sourceType = news.value("source_type", "");

			detailTasks.push_back(detail);
		}

		// 將詳情任務放入隊列
		for (const auto& dTask : detailTasks)
		{
			AddTask(dTask);
		}

		// 判斷下一步：切換股票還是翻頁
		if (hitOldNews || detailTasks.empty())
		{
			MoveNextStock(task);
			return;
		}
		else
		{
			SpiderTask nextNewsPageTask = task;
			nextNewsPageTask.type = SpiderTask::Type::StockNewsList;
			nextNewsPageTask.newsPageIndex++;

			AddTask(nextNewsPageTask);
		}
	}
	catch (const nlohmann::json::exception& e)
	{
		TRACE("ProcessStockNewsList JSON 解析異常: %s\n", e.what());
		MoveNextStock(task);
		return;
	}
	catch (const std::exception& e)
	{
		TRACE("ProcessStockNewsList 未知例外: %s\n", e.what());
		MoveNextStock(task);
		return;
	}

	ExecuteNextTask();
}

// ======================================================================
// 步驟 4: 公告詳情處理
// ======================================================================

void AStockNewsSpiderDlg::GetStockNewsDetail(const SpiderTask& task)
{
	CString artCode(task.artCode.c_str());
	CString url;

	url.Format(
		L"https://np-cnotice-stock.eastmoney.com/api/content/ann?"
		L"art_code=%s"
		L"&client_source=web"
		L"&page_index=1"
		L"&_=%ld",
		artCode,
		Utils::GetCurrentTimeMillis()
	);

	OpenUrl(ID_NEWS_DETAIL, url, task);
}

void AStockNewsSpiderDlg::ProcessStockNewsDetail(
	const SpiderTask& task,
	const std::string& responseData)
{
	try
	{
		auto jsonData = nlohmann::json::parse(responseData);

		std::string noticeDate = "";
		std::string noticeTitle = "";
		std::string noticeContent = "";
		std::string fileUrl = "";

		// 安全校驗 "data" 節點
		if (jsonData.is_object() && jsonData.contains("data") && jsonData["data"].is_object())
		{
			const auto& dataNode = jsonData["data"];

			noticeDate = dataNode.value("notice_date", "");
			noticeTitle = dataNode.value("notice_title", "");
			noticeContent = dataNode.value("notice_content", "");
			fileUrl = dataNode.value("attach_url", "");
		}
		else
		{
			TRACE("ProcessStockNewsDetail: API 回傳格式錯誤或 data 節點不存在 (artCode: %s)\n", task.artCode.c_str());
		}

		// 只有標題和內容不為空時才寫入資料庫
		if (!noticeTitle.empty() || !noticeContent.empty())
		{
			// 1. 寫入 TimescaleDB/PostgreSQL
			bool dbSuccess = PostgresManager::Instance().InsertNews(
				task.stockCode,
				task.stockName,
				task.artCode,
				task.annType,
				task.marketCode,
				task.sourceType,
				noticeDate,
				noticeTitle,
				noticeContent,
				fileUrl
			);

			// 2. 寫入成功後更新 SQLite 快取進度
			if (dbSuccess)
			{
				NewsCacheManager::Instance().UpdateLatestArtCode(task.stockCode, task.artCode);
			}
		}
		else
		{
			TRACE("公告詳情內容為空，跳過存庫 (artCode: %s)\n", task.artCode.c_str());
		}
	}
	catch (const nlohmann::json::exception& e)
	{
		TRACE("ProcessStockNewsDetail JSON 解析失敗 (artCode: %s): %s\n", task.artCode.c_str(), e.what());
	}
	catch (const std::exception& e)
	{
		TRACE("ProcessStockNewsDetail 未知例外 (artCode: %s): %s\n", task.artCode.c_str(), e.what());
	}

	// 無論是否成功解析或寫入，均調用 ExecuteNextTask 推進下一個任務
	ExecuteNextTask();
}
