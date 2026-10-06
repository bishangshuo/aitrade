// MFCApplication1Dlg.cpp: 实现文件
//

#include "pch.h"
#include "framework.h"
#include "AStockDataGrabber.h"
#include "AStockDataGrabberDlg.h"
#include "afxdialogex.h"
#include "Utils.h"

#include "RabbitPublisher.h"
#include "PostgresManager.h"

#include <nlohmann/json.hpp>   // 确保包含 JSON 解析库

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define WM_WEBVIEW2_RESPONSE (WM_USER + 100)
#define WM_CAPTCHA_TRIGGER     (WM_USER + 101)
#define WM_CAPTCHA_SOLVED      (WM_USER + 102)


const std::string ID_STOCK_LIST = "business-stocklist";
const std::string ID_STOCK_KLINE = "business-stockkline";
const std::string ID_BALANCE_SHEET = "business-balancesheet";
const std::string ID_INCOME_STATEMENT = "business-incomestatement";
const std::string ID_CASH_FLOW_STATEMENT = "business-cashflowstatement";

const int SLIDER_TO_BG_OFFSET_X = -28;  // 请根据实际测量调整
const int SLIDER_TO_BG_OFFSET_Y = -200;
const int BG_WIDTH = 264;
const int BG_HEIGHT = 162;

static int RandomDelay()
{
	static std::random_device rd;
	static std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dist(100, 200);
	return dist(gen);
}

/**
 * 根據 f100 欄位映射財報報表類型 (G / B / S / I)
 *
 * @param strF100 行業名稱（例如 _T("化學製品")、_T("IT服務Ⅱ")、_T("銀行")）
 * @return CString 報表模板字母代碼 (_T("G"), _T("B"), _T("S"), _T("I"))
 */
static CString MapToTemplateType(const CString& strF100)
{
	// 去除前後空格
	CString strIndustry = strF100;
	strIndustry.Trim();

	// 缺失行業數據或無效值時，預設回退為一般企業 (G)
	if (strIndustry.IsEmpty() || strIndustry == _T("-"))
	{
		return _T("G");
	}

	// 核心關鍵字判定 (Find 找不到時返回 -1)
	if (strIndustry.Find(_T("银行")) != -1)
	{
		return _T("B");
	}
	else if (strIndustry.Find(_T("证券")) != -1 || strIndustry.Find(_T("券商")) != -1)
	{
		return _T("S");
	}
	else if (strIndustry.Find(_T("保险")) != -1)
	{
		return _T("I");
	}

	// 其餘所有行業（化學製品、IT服務、房地產等）均為一般企業
	return _T("G");
}

// 用于应用程序“关于”菜单项的 CAboutDlg 对话框
class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

	// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()

// AStockDataGrabberDlg 对话框

AStockDataGrabberDlg::AStockDataGrabberDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_MFCAPPLICATION1_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	m_bNeedCaptchaSolver = FALSE;
	m_bCaptchaSolving = FALSE;
	m_bCaptchaSolved = FALSE;
	m_nCaptchaRetryCount = 0;
}

void AStockDataGrabberDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BUTTON_REQUEST, m_btnReq);
	DDX_Control(pDX, IDC_STATIC_CON, m_stCon);
}

BEGIN_MESSAGE_MAP(AStockDataGrabberDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_SIZE()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_REQUEST, &AStockDataGrabberDlg::OnBnClickedButtonRequest)
	ON_MESSAGE(WM_WEBVIEW2_RESPONSE, OnWebView2Response)
	ON_WM_TIMER()
	ON_MESSAGE(WM_CAPTCHA_TRIGGER, OnCaptchaTrigger)
	ON_MESSAGE(WM_CAPTCHA_SOLVED, OnCaptchaSolved)
END_MESSAGE_MAP()

// AStockDataGrabberDlg 消息处理程序

BOOL AStockDataGrabberDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	m_sliderTemplate = cv::imread("slider_template.png", cv::IMREAD_COLOR);
	if (m_sliderTemplate.empty()) {
		TRACE("警告：未加载滑块模板，请确保 slider_template.png 存在\n");
	}

	//读取上次保存的位置
	ReadPosition();

	PostgresManager::Instance().Initialize("host=192.168.0.60 port=5432 dbname=kline_db user=postgres password=123456");

	//初始化消息队列生产者
	RabbitPublisher::Instance().Start("192.168.0.60", 5672, "admin", "123456", "stock.data.exchange", "stock.data.routingKey");

	// 获取控件初始位置（客户区坐标）
	CRect rect;
	m_stCon.GetWindowRect(&rect);
	ScreenToClient(&rect);
	m_rectConInit = rect;

	m_nLeft = rect.left;
	m_nTop = rect.top;

	// 获取初始客户区大小
	CRect clientRect;
	GetClientRect(&clientRect);
	m_nRightMargin = clientRect.right - rect.right;   // 右边距
	m_nBottomMargin = clientRect.bottom - rect.bottom; // 下边距

	m_stCon.ModifyStyle(0, WS_CLIPCHILDREN);

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
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

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码

	 // 初始化 COM（WebView2 需要）
	CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);

	// 异步创建 WebView2 环境
	HRESULT hr = CreateCoreWebView2EnvironmentWithOptions(
		nullptr, nullptr, nullptr,
		Callback<ICoreWebView2CreateCoreWebView2EnvironmentCompletedHandler>(
			[this](HRESULT result, ICoreWebView2Environment* env) -> HRESULT {
				if (!env) return E_FAIL;
				m_webviewEnv = env;
				// 获取 ICoreWebView2Environment2 接口
				HRESULT hr2 = m_webviewEnv->QueryInterface(IID_PPV_ARGS(&m_webviewEnv2));
				if (FAILED(hr2)) {
					TRACE("获取 ICoreWebView2Environment2 失败: 0x%08X\n", hr2);
				}

				env->CreateCoreWebView2Controller(
					m_stCon.GetSafeHwnd(),
					Callback<ICoreWebView2CreateCoreWebView2ControllerCompletedHandler>(
						[this](HRESULT result, ICoreWebView2Controller* controller) -> HRESULT {
							if (!controller) return E_FAIL;
							m_webviewController = controller;
							m_webviewController->get_CoreWebView2(&m_webview);
							// 设置大小
							RECT rc;
							m_stCon.GetClientRect(&rc);
							m_webviewController->put_Bounds(rc);

							RegisterWebViewEvents();
							// 导航到目标 URL
							m_webview->Navigate(L"https://quote.eastmoney.com/center/gridlist.html#hs_a_board");
							return S_OK;
						}
					).Get()
				);
				return S_OK;
			}
		).Get()
	);

	if (FAILED(hr)) {
		// 错误处理
		AfxMessageBox(_T("Failed to create WebView2 environment"));
	}

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void AStockDataGrabberDlg::OnSize(UINT nType, int cx, int cy)
{
	CDialogEx::OnSize(nType, cx, cy);

	if (m_webviewController && m_stCon.GetSafeHwnd())
	{
		// 获取当前客户区
		CRect clientRect;
		GetClientRect(&clientRect);

		// 计算控件的新矩形：left, top 固定，right = 客户区宽度 - 右边距，bottom = 客户区高度 - 下边距
		CRect rectControl;
		rectControl.left = m_nLeft;
		rectControl.top = m_nTop;
		rectControl.right = clientRect.right - m_nRightMargin;
		rectControl.bottom = clientRect.bottom - m_nBottomMargin;

		// 如果希望控件最小宽度/高度，可以加限制
		if (rectControl.Width() < 100) rectControl.right = rectControl.left + 100;
		if (rectControl.Height() < 100) rectControl.bottom = rectControl.top + 100;

		m_stCon.MoveWindow(&rectControl);

		// 更新 WebView2 的大小
		CRect rectConClient;
		m_stCon.GetClientRect(&rectConClient);
		m_webviewController->put_Bounds(rectConClient);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void AStockDataGrabberDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR AStockDataGrabberDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void AStockDataGrabberDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

void AStockDataGrabberDlg::OnBnClickedButtonRequest()
{
	if (!m_webview) return;
	m_bCaptchaSolving = FALSE;
	GetStockList();
}

inline CString makeParam(int page)
{
	return NULL;
}

void AStockDataGrabberDlg::StartSpiderTimer()
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

void AStockDataGrabberDlg::OnTimer(UINT_PTR nIDEvent)
{
	if (nIDEvent == 1001)
	{
		KillTimer(m_spiderTimer);
		m_spiderTimer = 0;

		DoOpenUrl();

		return;
	}
	else if (nIDEvent == 1002) {
		KillTimer(1002);
		if (m_bNeedCaptchaSolver) {
			// 执行验证码处理
			HandleCaptcha();
		}
		return;
	}

	CDialogEx::OnTimer(nIDEvent);
}

bool AStockDataGrabberDlg::OpenUrl(const std::string& businessId, const CString& url)
{
	m_strCurrentBusinessId = businessId;
	m_strCurrentUrl = url;

	StartSpiderTimer();
	//DoOpenUrl();

	return true;
}

void AStockDataGrabberDlg::DoOpenUrl() {
	// 1. 基础防御：检查 URL 是否为空
	if (m_strCurrentUrl.IsEmpty()) {
		TRACE("OpenUrl 失败: URL 为空\n");
		return;
	}

	// 2. 获取 ICoreWebView2_3 接口
	ComPtr<ICoreWebView2_3> webview3;
	HRESULT hr = m_webview->QueryInterface(IID_PPV_ARGS(&webview3));
	if (FAILED(hr) || !webview3) {
		TRACE("获取 ICoreWebView2_3 接口失败: 0x%08X\n", hr);
		return;
	}

	// 3. 创建 WebResourceRequest
	ComPtr<ICoreWebView2WebResourceRequest> request;
	hr = m_webviewEnv2->CreateWebResourceRequest(
		m_strCurrentUrl.GetString(),  // URI (宽字符)
		L"GET",           // Method
		nullptr,          // postData (GET 请求传 nullptr)
		L"",              // Headers
		&request
	);
	if (FAILED(hr) || !request) {
		TRACE("创建 WebResourceRequest 失败: 0x%08X, URL: %s\n", hr, m_strCurrentUrl.GetString());
		return;
	}

	// 4. 添加自定义 Header
	ComPtr<ICoreWebView2HttpRequestHeaders> headers;
	hr = request->get_Headers(&headers);
	if (SUCCEEDED(hr) && headers) {
		CString cstrBusinessId(m_strCurrentBusinessId.c_str());
		headers->SetHeader(L"X-Business-ID", cstrBusinessId);
	}

	// 5. 执行导航
	hr = webview3->NavigateWithWebResourceRequest(request.Get());
	if (FAILED(hr)) {
		TRACE("NavigateWithWebResourceRequest 失败: 0x%08X\n", hr);
	}
}

void AStockDataGrabberDlg::GetStockList()
{
	if (!m_webview) return;

	CString strUrl;
	strUrl.Format(L"https://push2.eastmoney.com/weblogin/api/qt/clist/get?np=%d&fltt=%d&invt=%d&fs=%s&fields=%s&fid=%s&pn=%d&pz=%d&po=%d&dect=%d&ut=%s&wbp2u=%s&_=%ld",
		1, 
		1, 
		2,
		L"m:0+t:6+f:!2,m:0+t:80+f:!2,m:1+t:2+f:!2,m:1+t:23+f:!2,m:0+t:81+s:262144+f:!2",
		L"f12,f13,f14,f1,f2,f4,f3,f152,f5,f6,f7,f15,f18,f16,f17,f10,f8,f9,f23,f100", 
		"f3",
		m_nPage, 
		20, //页码，每页数量
		1, 
		1,
		L"fa5fd1943c7b386f172d6893dbfba10b",
		L"5140087827091262|0|1|0|web",
		Utils::GetCurrentTimeMillis());

	OpenUrl(ID_STOCK_LIST, strUrl);
}

void AStockDataGrabberDlg::GetStockKLine() //采集当前股票的K线数据
{
	//拿出第m_nStockIndex个股票
	auto& stock = m_listCurStock[m_nStockIndex];
	//平台ID：0:SZ 深证A股（或北证A股）， 1:SH 上证A股
	int platformId = stock["f13"].get<int>();
	std::string& stockId = stock["f12"].get<std::string>();
	CString cstrStockId(stockId.c_str());

	CString strUrl;
	strUrl.Format(L"https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=%d.%s&klt=101&fqt=1",
		0, cstrStockId//secid=0.600519
	);

	OpenUrl(ID_STOCK_KLINE, strUrl);
}

void AStockDataGrabberDlg::GetStockBalanceSheet(const std::string& businessId) //采集当前股票的资产负债表数据
{
	GetStockFinancialReport(businessId, 1);
}

void AStockDataGrabberDlg::GetStockIncomeStatement(const std::string& businessId) //采集当前股票的利润表数据
{
	GetStockFinancialReport(businessId, 2);
}

void AStockDataGrabberDlg::GetStockCashFlowStatement(const std::string& businessId) //采集当前股票的现金流量表数据
{
	GetStockFinancialReport(businessId, 3);
}

void AStockDataGrabberDlg::PrintCurrentStockInfo() 
{
	auto& stock = m_listCurStock[m_nStockIndex];
	std::string& stockId = stock["f12"].get<std::string>();
	//市场标识
	int marketId = stock["f13"].get<int>();
	std::string& marketName = Utils::getExchange(stockId, marketId);
	TRACE("当前股票信息: 股票代码=%s, 市场标识=%d, 市场名称=%s\n", stockId.c_str(), marketId, marketName.c_str());
}

/**
*fType:1资产负债表，2利润表，3现金流量表
*/
void AStockDataGrabberDlg::GetStockFinancialReport(const std::string& businessId, int fType)
{
	// 拿出第m_nStockIndex个股票
	auto& stock = m_listCurStock[m_nStockIndex];
	std::string& stockId = stock["f12"].get<std::string>();
	//市场标识
	int marketId = stock["f13"].get<int>();
	std::string& marketName = Utils::getExchange(stockId, marketId);

	std::string& f100 = stock["f100"].get<std::string>();
	CString csF100 = CA2W(f100.c_str(), CP_UTF8);
	CString f100_pre = MapToTemplateType(csF100);
	CString cstrType = _T("";)
	CString cstrSty = _T("");
	if (1 == fType)
	{
		cstrType.Format(_T("RPT_F10_FINANCE_%sBALANCE"), f100_pre);
		cstrSty.Format(_T("F10_FINANCE_%sBALANCE"), f100_pre);
	} else if(2 == fType)
	{
		cstrType.Format(_T("RPT_F10_FINANCE_%sINCOME"), f100_pre);
		cstrSty.Format(_T("APP_F10_%sINCOME"), f100_pre);
	}
	else if (3 == fType)
	{
		cstrType.Format(_T("RPT_F10_FINANCE_%sCASHFLOW"), f100_pre);
		cstrSty.Format(_T("APP_F10_%sCASHFLOW"), f100_pre);
	}

	// 生成一个api调用的版本字符串，格式03382986918773889
	std::string& version = Utils::generateNumericVersion(17);

	// 季报日期，全部类型，默认 最新的财报日期倒推12个季度，共三年
	// 改用5年，21个季度
	int seasons = 21;
	std::string& reportDate = Utils::GetLastQuarterEndList(seasons);

	// 【核心修改】：将 std::string 先转换为 CString
	// CString 的构造函数能够安全地将 const char* 转换为宽字符（在 Unicode 环境下）
	CString cstrStockId(stockId.c_str());
	CString cstrMarketName(marketName.c_str());
	CString cstrReportDate(reportDate.c_str());
	CString cstrVersion(version.c_str());

	CString strUrl;
	// 在 Unicode 环境下，%s 对应 CString（即 const wchar_t*）
	strUrl.Format(L"https://datacenter.eastmoney.com/securities/api/data/get?type=%s&sty=%s&filter=(SECUCODE=\"%s.%s\")(REPORT_DATE in (%s))&p=%d&ps=%d&sr=%d&st=%s&source=%s&client=%s&v=%s",
		cstrType,         // 传入 CString 对象，自动调用 operator LPCWSTR()
		cstrSty,
		cstrStockId,
		cstrMarketName,
		cstrReportDate,
		1,
		40,
		-1,
		L"REPORT_DATE",
		L"HSF10",
		L"PC",
		cstrVersion
	);

	std::wcout << "GetStockFinancialReport url=" << (const wchar_t*)strUrl << std::endl;

	OpenUrl(businessId, strUrl);
}


void AStockDataGrabberDlg::RegisterWebViewEvents()
{
	if (!m_webview) return;

	// 添加过滤器：拦截所有发往东方财富 API 的请求
	m_webview->AddWebResourceRequestedFilter(
		L"*quote.eastmoney.com*",
		COREWEBVIEW2_WEB_RESOURCE_CONTEXT_ALL
	);

	// 注册请求到达事件（可以获取 URI、修改请求头等）
	EventRegistrationToken requestToken;
	HRESULT hr = m_webview->add_WebResourceRequested(
		Microsoft::WRL::Callback<ICoreWebView2WebResourceRequestedEventHandler>(
			[this](ICoreWebView2* sender, ICoreWebView2WebResourceRequestedEventArgs* args) -> HRESULT {
				ComPtr<ICoreWebView2WebResourceRequest> request;
				args->get_Request(&request);

				LPWSTR uri = nullptr;
				request->get_Uri(&uri);
				if (uri) {
					OutputDebugString(L"拦截到请求: ");
					OutputDebugString(uri);
					OutputDebugString(L"\n");
					CoTaskMemFree(uri); // 释放 COM 分配的字符串
				}
				// 此处可以修改请求头或记录日志
				return S_OK;
			}
		).Get(),
		&requestToken
	);

	if (FAILED(hr)) {
		TRACE("add_WebResourceRequested 失败: 0x%08X\n", hr);
	}

	EventRegistrationToken navCompletedToken;
	hr = m_webview->add_NavigationCompleted(
		Callback<ICoreWebView2NavigationCompletedEventHandler>(
			[this](ICoreWebView2* sender, ICoreWebView2NavigationCompletedEventArgs* args) -> HRESULT {
				BOOL success = FALSE;
				args->get_IsSuccess(&success);
				if (!success) {
					// 获取导航失败原因
					COREWEBVIEW2_WEB_ERROR_STATUS errorStatus;
					args->get_WebErrorStatus(&errorStatus);
					// 触发验证码或重试
					::PostMessage(GetSafeHwnd(), WM_CAPTCHA_TRIGGER, 0, 0);
				}
				else {
					// 检查是否导航到主页
					LPWSTR uri = nullptr;
					sender->get_Source(&uri);
					if (uri) {
						std::wstring wuri(uri);
						CoTaskMemFree(uri);
						if (wuri.find(L"quote.eastmoney.com/center/gridlist.html") != std::wstring::npos) {
							// 如果需要处理验证码
							if (m_bNeedCaptchaSolver) {
								// 延迟一点等待页面渲染
								SetTimer(1002, 1000, nullptr); // 定时处理验证码
							}
						}
					}
				}
				return S_OK;
			}
		).Get(),
		&navCompletedToken
	);

	if (FAILED(hr)) {
		TRACE("add_NavigationCompleted 失败: 0x%08X\n", hr);
	}

	// 如果需要获取响应体，可使用 ICoreWebView2_4 接口（可选）
	// 以下代码暂时注释，以免引入额外错误
	// 1. 获取支持 WebResourceResponseReceived 的接口
	ComPtr<ICoreWebView2_2> m_webview2;
	hr = m_webview->QueryInterface(IID_PPV_ARGS(&m_webview2));
	if (FAILED(hr) || !m_webview2) {
		TRACE("无法获取 ICoreWebView2_2 接口\n");
		return;
	}

	// 2. 注册 WebResourceResponseReceived 事件
	EventRegistrationToken responseToken;
	// 在 RegisterWebViewEvents 中的 WebResourceResponseReceived 回调
	hr = m_webview2->add_WebResourceResponseReceived(
		Callback<ICoreWebView2WebResourceResponseReceivedEventHandler>(
			[this](
				ICoreWebView2* sender,
				ICoreWebView2WebResourceResponseReceivedEventArgs* args) -> HRESULT {
					// 获取请求对象
					ComPtr<ICoreWebView2WebResourceRequest> request;
					args->get_Request(&request);
					if (!request) return S_OK;

					// 读取业务标识
					ComPtr<ICoreWebView2HttpRequestHeaders> headers;
					request->get_Headers(&headers);
					std::string businessId;
					if (headers) {
						LPWSTR id = nullptr;
						headers->GetHeader(L"X-Business-ID", &id);
						if (id) {
							businessId = CW2A(id, CP_UTF8);
							CoTaskMemFree(id);
						}
					}

					// 获取响应对象
					ComPtr<ICoreWebView2WebResourceResponseView> responseView;
					args->get_Response(&responseView);
					if (!responseView) return S_OK;

					// 获取响应状态码
					int statusCode = 0;
					responseView->get_StatusCode(&statusCode);

					// 如果状态码不是 200，触发验证码（或重试）
					if (statusCode != 200 && !businessId.empty()) {
						TRACE("API 返回状态码 %d，触发验证码流程\n", statusCode);
						::PostMessage(GetSafeHwnd(), WM_CAPTCHA_TRIGGER, 0, 0);
						return S_OK;
					}

					// 状态码为 200，异步获取响应内容
					responseView->GetContent(
						Callback<ICoreWebView2WebResourceResponseViewGetContentCompletedHandler>(
							[this, businessId](HRESULT result, IStream* contentStream) -> HRESULT {
								// 如果获取内容失败或流为空，也触发验证码
								if (FAILED(result) || !contentStream) {
									return S_OK;
								}

								// 读取内容（原有逻辑）
								STATSTG stat;
								contentStream->Stat(&stat, STATFLAG_NONAME);
								ULONGLONG size = stat.cbSize.QuadPart;
								if (size > 0) {
									std::vector<BYTE> buffer((size_t)size);
									ULONG bytesRead = 0;
									contentStream->Read(buffer.data(), (ULONG)size, &bytesRead);
									std::string responseData((char*)buffer.data(), bytesRead);

									// 将数据 + 业务标识打包发送到主线程
									std::pair<std::string, std::string>* pData =
										new std::pair<std::string, std::string>(businessId, responseData);
									::PostMessage(GetSafeHwnd(), WM_WEBVIEW2_RESPONSE, 0, (LPARAM)pData);
								}
								else {
									TRACE("API 响应内容为空\n");
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
		TRACE("注册 WebResourceResponseReceived 失败: 0x%08X\n", hr);
	}
}

bool inline _is_business_id(const std::string& businessId) {
	return (businessId == ID_STOCK_LIST ||
		businessId == ID_STOCK_KLINE ||
		businessId == ID_BALANCE_SHEET ||
		businessId == ID_INCOME_STATEMENT ||
		businessId == ID_CASH_FLOW_STATEMENT);
}

LRESULT AStockDataGrabberDlg::OnWebView2Response(WPARAM wParam, LPARAM lParam)
{
	auto* pData = reinterpret_cast<std::pair< std::string, std::string>*>(lParam);
	if (pData) {
		std::string businessId = pData->first;
		std::string& responseData = pData->second;

		if(!_is_business_id(businessId)) {
			delete pData;
			return 0;
		}

		std::cout << "OnWebView2Response businessId=" << businessId << ", responseData=" << responseData << std::endl;

		// 根据业务标识进行处理
		if (businessId == ID_STOCK_LIST) {
			ProcessStockList(responseData);
		}
		else if (businessId == ID_STOCK_KLINE) {
			ProcessStockKLine(businessId, responseData);
		}
		else if (businessId == ID_BALANCE_SHEET) {
			ProcessBalanceSheet(businessId, responseData);
		}
		else if (businessId == ID_INCOME_STATEMENT) {
			ProcessIncomeStatement(businessId, responseData);
		}
		else if (businessId == ID_CASH_FLOW_STATEMENT) {
			ProcessCashFlowStatement(businessId, responseData);
		}

		delete pData;
	}
	else {
		OutputDebugString(L"OnWebView2Response: pData 为 nullptr\n");
	}
	return 0;
}

bool AStockDataGrabberDlg::StockListEmpty(const nlohmann::json& jsonData)
{
	if (nullptr == jsonData)
		return true;
	if (!jsonData.contains("data"))
		return true;
	if (!jsonData["data"].contains("diff"))
		return true;
	if (!jsonData["data"]["diff"].is_array())
		return true;
	if (jsonData["data"]["diff"].empty())
		return true;
	return false;
}

void AStockDataGrabberDlg::ProcessStockList(const std::string& responseData)
{
	try {
		nlohmann::json& jsonData = nlohmann::json::parse(responseData);
		if (jsonData == nullptr || jsonData.empty() || StockListEmpty(jsonData)) {
			m_nPage = 1;
			GetStockList();
			return;
		}

		//股票列表
		m_listCurStock = jsonData["data"]["diff"];
		m_nStockIndex = 0;

		// 处理当前股票列表里的第一个股票
		GetStockData();
	}
	catch (...) {
		TRACE("ProcessStockList 解析 JSON 失败\n");
	}
}

void AStockDataGrabberDlg::SendRabbitMqMessage(const std::string& businessId, const std::string& responseData)
{
	// 创建一个带有业务标识的 JSON 对象， 格式： {"businessId": "business-stocklist", "data": responseData }
	nlohmann::json jsonData;

	jsonData["businessId"] = businessId;
	jsonData["data"] = nlohmann::json::parse(responseData);

	std::string dataStr = jsonData.dump();

	RabbitPublisher::Instance().Publish(dataStr);
}

inline bool isEmptyStockKline(const nlohmann::json& jsonData) {
	if(nullptr == jsonData)
		return true;
	if(jsonData.empty())
		return true;
	if (!jsonData.contains("data"))
		return true;
	if(!jsonData["data"].contains("klines"))
		return true;
	return false;
}

void AStockDataGrabberDlg::ProcessStockKLine(const std::string& businessId, const std::string& responseData)
{
	try {
		const nlohmann::json& jsonData = nlohmann::json::parse(responseData);
		if (isEmptyStockKline(jsonData)) {
			GetStockBalanceSheet(ID_BALANCE_SHEET);
			return;
		}

		//SendRabbitMqMessage(businessId, responseData);
		auto& stock = m_listCurStock[m_nStockIndex];
		std::string& stCode = stock["f12"].get<std::string>();
		std::string& stName = stock["f14"].get<std::string>();
		//市场标识
		int marketId = stock["f13"].get<int>();

		const std::vector<std::string>& rawDataList = jsonData["data"]["klines"];
		PostgresManager::Instance().BatchInsertKlineDay(stCode, stName, marketId, rawDataList);		
		GetStockBalanceSheet(ID_BALANCE_SHEET);
	}
	catch (...) {
		TRACE("ProcessStockKLine 解析 JSON 失败\n");
	}
}

bool AStockDataGrabberDlg::IsGetFinanceDataSuccess(const nlohmann::json& jsonData)
{
	if (nullptr == jsonData)
		return false;
	int code = -1;
	if (jsonData.contains("code"))
		code = jsonData["code"].get<int>();

	bool success = false;
	if (jsonData.contains("success"))
		success = jsonData["success"].get<bool>();

	bool hasResult = jsonData.contains("result");
	if(hasResult && jsonData["result"].contains("data")) {
		auto& data = jsonData["result"]["data"];
		if (data.is_array() && data.empty()) {
			hasResult = false;
		}
	}

	return (success && hasResult && code == 0);
}

void AStockDataGrabberDlg::ProcessBalanceSheet(const std::string& businessId, const std::string& responseData)
{
	try {
		nlohmann::json& jsonData = nlohmann::json::parse(responseData);
		if (IsGetFinanceDataSuccess(jsonData)) {
			PostgresManager::Instance().InsertBalanceSheet(jsonData["result"]["data"]);
		}
		else {
			TRACE("ProcessBalanceSheet 获取数据失败\n");
			PrintCurrentStockInfo();
		}
		GetStockIncomeStatement(ID_INCOME_STATEMENT);
	}
	catch (...) {
		TRACE("ProcessBalanceSheet 解析 JSON 失败\n");
	}
}

void AStockDataGrabberDlg::ProcessIncomeStatement(const std::string& businessId, const std::string& responseData)
{
	try {
		nlohmann::json& jsonData = nlohmann::json::parse(responseData);
		
		if (IsGetFinanceDataSuccess(jsonData)) {
			PostgresManager::Instance().InsertIncomeStatement(jsonData["result"]["data"]);
		}
		else {
			TRACE("ProcessIncomeStatement 获取数据失败\n");
			PrintCurrentStockInfo();
		}
		GetStockCashFlowStatement(ID_CASH_FLOW_STATEMENT);
	}
	catch (...) {
		TRACE("ProcessIncomeStatement 解析 JSON 失败\n");
	}
}

void AStockDataGrabberDlg::ProcessCashFlowStatement(const std::string& businessId, const std::string& responseData)
{
	try {
		nlohmann::json& jsonData = nlohmann::json::parse(responseData);
		
		if (IsGetFinanceDataSuccess(jsonData)) {
			PostgresManager::Instance().InsertCashFlowStatement(jsonData["result"]["data"]);
		}
		else {
			TRACE("ProcessCashFlowStatement 获取数据失败\n");
			PrintCurrentStockInfo();
		}

		//记录当前采集位置
		MarkPosition();

		m_nStockIndex++;
		if (m_nStockIndex < m_listCurStock.size())
		{
			GetStockData();
		}
		else
		{
			m_nStockIndex = 0;
			m_nPage++;
			GetStockList();
		}
	}
	catch (...) {
		TRACE("ProcessCashFlowStatement 解析 JSON 失败\n");
	}
}

void AStockDataGrabberDlg::GetStockData()
{
	//以此采集股票的K线、财报、新闻数据，在k线采集结果回调里接着采集财报数据，在财报采集结果回调里接着采集新闻数据
	//GetStockKLine();
	GetStockBalanceSheet(ID_BALANCE_SHEET);
}

void AStockDataGrabberDlg::MarkPosition()
{
	CStdioFile file;
	// 以文本模式、创建或覆盖的方式打开文件
	if (file.Open(_T("position.txt"), CFile::modeCreate | CFile::modeWrite | CFile::typeText))
	{
		CString strLine;

		strLine.Format(_T("page=%d"), m_nPage);
		file.WriteString(strLine + _T("\n"));

		strLine.Format(_T("index=%d"), m_nStockIndex);
		file.WriteString(strLine + _T("\n"));

		file.Close();
	}
	else
	{
		// 可选：处理打开文件失败的情况
		AfxMessageBox(_T("无法保存位置信息！"));
	}
}
void AStockDataGrabberDlg::ReadPosition()
{
	CStdioFile file;
	// 以文本模式、只读方式打开文件
	if (file.Open(_T("position.txt"), CFile::modeRead | CFile::typeText))
	{
		CString strLine;
		while (file.ReadString(strLine))
		{
			// 去除可能存在的回车符（ReadString 会保留 \r）
			strLine.Trim();

			if (strLine.IsEmpty()) continue;

			int nPos = strLine.Find(_T('='));
			if (nPos == -1) continue; // 没有 '=' 则跳过

			CString strKey = strLine.Left(nPos).Trim();
			CString strVal = strLine.Mid(nPos + 1).Trim();

			if (strKey == _T("page"))
			{
				m_nPage = _ttoi(strVal); // 或者使用 _tcstol 等更安全的转换
			}
			else if (strKey == _T("index"))
			{
				m_nStockIndex = _ttoi(strVal);
			}
		}
		file.Close();
	}
	else
	{
		// 文件不存在或读取失败，给成员变量设置默认值
		m_nPage = 1;
		m_nStockIndex = 0;
	}
}

// ---------- 验证码触发与解决 ----------
LRESULT AStockDataGrabberDlg::OnCaptchaTrigger(WPARAM wParam, LPARAM lParam)
{
	if (m_bCaptchaSolving) return 0;
	m_bNeedCaptchaSolver = TRUE;
	m_bCaptchaSolving = TRUE;
	m_nCaptchaRetryCount = 0;
	if (m_webview) {
		m_webview->Navigate(L"https://quote.eastmoney.com/center/gridlist.html#hs_a_board");
	}
	return 0;
}

LRESULT AStockDataGrabberDlg::OnCaptchaSolved(WPARAM wParam, LPARAM lParam)
{
	m_bNeedCaptchaSolver = FALSE;
	m_bCaptchaSolving = FALSE;
	m_bCaptchaSolved = TRUE;
	m_nCaptchaRetryCount = 0;
	TRACE(_T("滑块验证码已成功解决，继续采集任务\n"));
	ContinueCollection();
	return 0;
}

// ---------- 图像处理辅助函数 ----------
bool AStockDataGrabberDlg::FindSliderByTemplate(const cv::Mat& image, cv::Point& center, double threshold)
{
	if (image.empty() || m_sliderTemplate.empty()) {
		TRACE("图像或模板为空\n");
		return false;
	}

	// 先保存一个调试截图，方便检查
	// cv::imwrite("debug_captcha_full.png", image);

	// 尝试多个缩放比例（应对 DPI 变化）
	std::vector<double> scales = { 1.0, 0.9, 0.8, 1.1, 1.2 };
	double bestVal = -1.0;
	cv::Point bestLoc;

	for (double scale : scales) {
		cv::Mat scaledTemplate;
		if (scale != 1.0) {
			int newW = (int)(m_sliderTemplate.cols * scale);
			int newH = (int)(m_sliderTemplate.rows * scale);
			if (newW < 10 || newH < 10) continue;
			cv::resize(m_sliderTemplate, scaledTemplate, cv::Size(newW, newH), 0, 0, cv::INTER_AREA);
		}
		else {
			scaledTemplate = m_sliderTemplate;
		}

		cv::Mat result;
		cv::matchTemplate(image, scaledTemplate, result, cv::TM_CCOEFF_NORMED);
		double minVal, maxVal;
		cv::Point minLoc, maxLoc;
		cv::minMaxLoc(result, &minVal, &maxVal, &minLoc, &maxLoc);

		if (maxVal > bestVal) {
			bestVal = maxVal;
			bestLoc = maxLoc;
			// 记录最佳匹配的模板尺寸
		}
	}

	TRACE("最佳匹配得分: %f (阈值: %f)\n", bestVal, threshold);

	if (bestVal < threshold) {
		// 可在此保存匹配结果供调试
		// cv::imwrite("debug_match_result.png", result);
		return false;
	}

	// 注意：此处使用最佳匹配的位置，但模板尺寸是原始尺寸还是缩放后的？
	// 为了简化，我们使用原始模板尺寸作为偏移，但实际上应使用匹配时的模板尺寸。
	// 由于我们只关心中心点，并且缩放变化不大，可用原始尺寸近似。
	// 更准确：存储匹配时的模板尺寸。
	// 这里用原始尺寸，若缩放较大可能需要调整。
	center.x = bestLoc.x + m_sliderTemplate.cols / 2;
	center.y = bestLoc.y + m_sliderTemplate.rows / 2;

	return true;
}

int AStockDataGrabberDlg::LocateGapPosition(const cv::Mat& srcImage)
{
	if (srcImage.empty())
		return -1;

	cv::Mat gray;
	if (srcImage.channels() == 3)
		cv::cvtColor(srcImage, gray, cv::COLOR_BGR2GRAY);
	else
		gray = srcImage.clone();

	const int width = gray.cols;
	const int height = gray.rows;

	/*
	 * 你的缺口位于图片右下区域。
	 *
	 * 不要在整张图片上检测。
	 * 直接限定 ROI，可以大幅降低树叶纹理对检测的干扰。
	 *
	 * X: 40% ~ 75%
	 * Y: 55% ~ 95%
	 */
	int roiX1 = static_cast<int>(width * 0.35);
	int roiX2 = static_cast<int>(width * 0.75);

	int roiY1 = static_cast<int>(height * 0.55);
	int roiY2 = static_cast<int>(height * 0.95);

	roiX1 = std::max(0, roiX1);
	roiX2 = std::min(width, roiX2);
	roiY1 = std::max(0, roiY1);
	roiY2 = std::min(height, roiY2);

	if (roiX2 <= roiX1 || roiY2 <= roiY1)
		return -1;

	cv::Mat roi = gray(cv::Range(roiY1, roiY2),
		cv::Range(roiX1, roiX2));

	/*
	 * ---------------------------------------------------------
	 * 1. 对 ROI 做轻度高斯模糊
	 *
	 * 树叶纹理很多，不能直接使用原始灰度。
	 * 模糊以后，保留“缺口的大面积暗区域”，
	 * 同时削弱树叶的小尺度纹理。
	 * ---------------------------------------------------------
	 */
	cv::Mat blurImg;
	cv::GaussianBlur(
		roi,
		blurImg,
		cv::Size(9, 9),
		0
	);

	/*
	 * ---------------------------------------------------------
	 * 2. 计算每一列的平均亮度
	 *
	 * 缺口内部整体比较暗，因此：
	 *
	 *        正常背景       缺口
	 *          ↓             ↓
	 *
	 *       100  110  80  40  40  45  50  100
	 *                   ↑
	 *                左边界
	 *
	 * 我们实际上寻找的是：
	 *
	 *     从亮 -> 暗
	 *
	 * 的明显下降。
	 * ---------------------------------------------------------
	 */
	std::vector<double> colMean(blurImg.cols, 0.0);

	for (int x = 0; x < blurImg.cols; ++x)
	{
		cv::Scalar meanVal = cv::mean(
			blurImg.col(x)
		);

		colMean[x] = meanVal[0];
	}

	/*
	 * ---------------------------------------------------------
	 * 3. 再进行一次一维平滑
	 * ---------------------------------------------------------
	 */
	std::vector<double> smooth(colMean.size(), 0.0);

	const int smoothRadius = 4;

	for (int x = 0; x < static_cast<int>(colMean.size()); ++x)
	{
		double sum = 0.0;
		int count = 0;

		for (int dx = -smoothRadius;
			dx <= smoothRadius;
			++dx)
		{
			int xx = x + dx;

			if (xx >= 0 && xx < static_cast<int>(colMean.size()))
			{
				sum += colMean[xx];
				++count;
			}
		}

		smooth[x] = sum / count;
	}

	/*
	 * ---------------------------------------------------------
	 * 4. 找“亮 -> 暗”的下降边界
	 *
	 * 左边界：
	 *
	 *       背景
	 *        |
	 *        |\
	 *        | \
	 *        |  \______
	 *        |         缺口
	 *        |
	 *
	 * 所以寻找：
	 *
	 *     smooth[x] - smooth[x+N]
	 *
	 * 最大的位置。
	 * ---------------------------------------------------------
	 */

	double bestScore = -DBL_MAX;
	int bestX = -1;

	// 至少需要比较 4 个像素
	const int lookAhead = 5;

	for (int x = 2;
		x < static_cast<int>(smooth.size()) - lookAhead - 2;
		++x)
	{
		/*
		 * 左侧平均亮度
		 */
		double leftValue =
			(smooth[x - 2] +
				smooth[x - 1] +
				smooth[x]) / 3.0;

		/*
		 * 右侧平均亮度
		 */
		double rightValue =
			(smooth[x + lookAhead] +
				smooth[x + lookAhead + 1]) / 2.0;

		/*
		 * 越大说明：
		 *
		 *     左边亮
		 *     右边暗
		 *
		 * 越可能是缺口左边界。
		 */
		double score = leftValue - rightValue;

		if (score > bestScore)
		{
			bestScore = score;
			bestX = x;
		}
	}

	if (bestX < 0)
		return -1;

	/*
	 * ---------------------------------------------------------
	 * 5. 把 ROI 坐标转换成原图坐标
	 * ---------------------------------------------------------
	 */
	int gapLeftX = roiX1 + bestX;

	/*
	 * ---------------------------------------------------------
	 * 6. 进一步做一个暗区验证
	 *
	 * 防止树叶某一处刚好出现很强的亮暗变化。
	 *
	 * 如果 bestX 真的是缺口左边界，
	 * 那么它右边应该存在一段连续的暗区域。
	 * ---------------------------------------------------------
	 */

	int darkWidth = 12;

	int x1 = bestX + 3;
	int x2 = std::min(
		bestX + darkWidth,
		static_cast<int>(smooth.size()) - 1
	);

	if (x2 > x1)
	{
		double darkMean = 0.0;

		for (int x = x1; x <= x2; ++x)
		{
			darkMean += smooth[x];
		}

		darkMean /= (x2 - x1 + 1);

		/*
		 * 如果右边完全不暗，
		 * 则很可能不是缺口。
		 *
		 * 这个阈值不要设置得太低。
		 */
		if (darkMean > 100.0)
		{
			return -1;
		}
	}

	return gapLeftX;
}

cv::Mat AStockDataGrabberDlg::CaptureWebViewSync()
{
	if (!m_webview) return cv::Mat();

	// 确保 WebView 控件尺寸有效
	CRect rect;
	m_stCon.GetClientRect(&rect);
	if (rect.Width() <= 0 || rect.Height() <= 0) {
		TRACE("控件尺寸无效\n");
		return cv::Mat();
	}
	m_webviewController->put_Bounds(rect);

	// 等待一帧渲染
	::Sleep(100);

	const int MAX_ATTEMPTS = 5;
	for (int attempt = 0; attempt < MAX_ATTEMPTS; ++attempt) {
		CComPtr<IStream> stream;
		HRESULT hr = ::CreateStreamOnHGlobal(NULL, TRUE, &stream);
		if (FAILED(hr)) {
			TRACE("CreateStreamOnHGlobal 失败: 0x%08X\n", hr);
			continue;
		}

		HANDLE hEvent = ::CreateEvent(NULL, TRUE, FALSE, NULL);
		if (!hEvent) continue;

		HRESULT captureResult = S_OK;
		hr = m_webview->CapturePreview(
			COREWEBVIEW2_CAPTURE_PREVIEW_IMAGE_FORMAT_PNG,
			stream,
			Microsoft::WRL::Callback<ICoreWebView2CapturePreviewCompletedHandler>(
				[hEvent, &captureResult](HRESULT errorCode) -> HRESULT {
					captureResult = errorCode;
					::SetEvent(hEvent);
					return S_OK;
				}
			).Get()
		);

		//if (FAILED(hr)) {
		//	::CloseHandle(hEvent);
		//	TRACE("CapturePreview 调用失败: 0x%08X\n", hr);
		//	continue;
		//}

		// 等待事件（最多 3 秒）
		DWORD waitResult = ::WaitForSingleObject(hEvent, 300000);
		::CloseHandle(hEvent);

		if (waitResult != WAIT_OBJECT_0) {
			TRACE("CapturePreview 超时\n");
			continue;
		}

		if (FAILED(captureResult)) {
			TRACE("CapturePreview 异步失败: 0x%08X\n", captureResult);
			continue;
		}

		// 读取流
		STATSTG stat = { 0 };
		hr = stream->Stat(&stat, STATFLAG_NONAME);
		if (FAILED(hr) || stat.cbSize.QuadPart == 0) {
			TRACE("截图流为空，重试...\n");
			::Sleep(200); // 短暂等待再重试
			continue;
		}

		ULONGLONG size = stat.cbSize.QuadPart;
		std::vector<BYTE> buffer((size_t)size);
		ULONG bytesRead = 0;
		LARGE_INTEGER zero = { 0 };
		stream->Seek(zero, STREAM_SEEK_SET, NULL);
		stream->Read(buffer.data(), (ULONG)size, &bytesRead);
		if (bytesRead == 0) continue;

		cv::Mat img = cv::imdecode(buffer, cv::IMREAD_COLOR);
		if (!img.empty()) {
			TRACE("CapturePreview 截图成功，尺寸 %dx%d\n", img.cols, img.rows);
			return img;
		}
	}

	TRACE("所有 CapturePreview 尝试失败\n");
	return cv::Mat();
}

/**
 * @brief 异步将 WebView2 当前页面截图并转换为 cv::Mat
 * @param webView2 WebView2 核心接口指针
 * @param callback 截图完成后的回调函数，参数为解码后的 cv::Mat (若失败则为空)
 */
 /**
  * @brief 异步将 WebView2 当前页面截图并转换为 cv::Mat
  * @param webView2 WebView2 核心接口指针
  * @param callback 截图完成后的回调函数，参数为解码后的 cv::Mat (若失败则为空)
  */
cv::Mat AStockDataGrabberDlg::CaptureWebView2ToMatSync(const Microsoft::WRL::ComPtr<ICoreWebView2>& webView2)
{
	if (!webView2) return cv::Mat();

	HANDLE hEvent = CreateEvent(nullptr, TRUE, FALSE, nullptr);
	if (!hEvent) return cv::Mat();

	Microsoft::WRL::ComPtr<IStream> stream(SHCreateMemStream(nullptr, 0));
	cv::Mat resultMat;

	if (stream) {
		// 修复点1：C++ Win32 API 的方法名是 CapturePreview，不是 CapturePreviewAsync
		HRESULT hr = webView2->CapturePreview(
			COREWEBVIEW2_CAPTURE_PREVIEW_IMAGE_FORMAT_PNG,
			stream.Get(),
			// 修复点2：补全 Lambda 参数列表，让 WRL 能够正确推导回调类型
			Microsoft::WRL::Callback<ICoreWebView2CapturePreviewCompletedHandler>(
				[stream, &resultMat, hEvent](HRESULT errorCode) -> HRESULT
				{
					if (SUCCEEDED(errorCode)) {
						STATSTG stat;
						if (SUCCEEDED(stream->Stat(&stat, STATFLAG_NONAME))) {
							std::vector<BYTE> buffer(static_cast<size_t>(stat.cbSize.QuadPart));
							LARGE_INTEGER pos = { 0 };
							stream->Seek(pos, STREAM_SEEK_SET, nullptr);

							ULONG bytesRead = 0;
							if (SUCCEEDED(stream->Read(buffer.data(),
								static_cast<ULONG>(buffer.size()), &bytesRead)) &&
								bytesRead == buffer.size())
							{
								resultMat = cv::imdecode(buffer, cv::IMREAD_COLOR);
							}
						}
					}

					SetEvent(hEvent);
					return S_OK;
				}
			).Get()
		);

		if (FAILED(hr)) {
			SetEvent(hEvent);
		}
	}
	else {
		SetEvent(hEvent);
	}

	// 带消息泵的等待循环，防止主线程死锁
	while (true) {
		DWORD waitResult = MsgWaitForMultipleObjects(
			1, &hEvent, FALSE, INFINITE, QS_ALLINPUT);

		if (waitResult == WAIT_OBJECT_0) {
			break; // 截图完成
		}
		else if (waitResult == WAIT_OBJECT_0 + 1) {
			MSG msg;
			while (PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE)) {
				TranslateMessage(&msg);
				DispatchMessage(&msg);
			}
		}
		else {
			break;
		}
	}

	CloseHandle(hEvent);
	return resultMat;
}

//cv::Mat AStockDataGrabberDlg::CaptureWebViewGDI()
//{
//	if (!m_webviewController) return cv::Mat();
//	// 获取 WebView2 控件的 HWND
//	HWND hWnd = m_stCon.GetSafeHwnd();
//	if (!hWnd) return cv::Mat();
//
//	// 获取控件大小
//	RECT rc;
//	::GetClientRect(hWnd, &rc);
//	int width = rc.right - rc.left;
//	int height = rc.bottom - rc.top;
//	if (width <= 0 || height <= 0) return cv::Mat();
//
//	// 创建兼容 DC 和位图
//	HDC hdcScreen = ::GetDC(hWnd);
//	HDC hdcMem = ::CreateCompatibleDC(hdcScreen);
//	HBITMAP hBitmap = ::CreateCompatibleBitmap(hdcScreen, width, height);
//	HGDIOBJ hOld = ::SelectObject(hdcMem, hBitmap);
//
//	// 复制窗口客户区
//	::BitBlt(hdcMem, 0, 0, width, height, hdcScreen, 0, 0, SRCCOPY);
//
//	// 取得位图数据
//	BITMAPINFO bmi = { 0 };
//	bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
//	bmi.bmiHeader.biWidth = width;
//	bmi.bmiHeader.biHeight = -height; // 倒置以使数据为正
//	bmi.bmiHeader.biPlanes = 1;
//	bmi.bmiHeader.biBitCount = 24;     // 24位彩色
//	bmi.bmiHeader.biCompression = BI_RGB;
//
//	std::vector<BYTE> buffer(width * height * 3);
//	if (!::GetDIBits(hdcMem, hBitmap, 0, height, buffer.data(), &bmi, DIB_RGB_COLORS)) {
//		::SelectObject(hdcMem, hOld);
//		::DeleteObject(hBitmap);
//		::DeleteDC(hdcMem);
//		::ReleaseDC(hWnd, hdcScreen);
//		return cv::Mat();
//	}
//
//	// 清理
//	::SelectObject(hdcMem, hOld);
//	::DeleteObject(hBitmap);
//	::DeleteDC(hdcMem);
//	::ReleaseDC(hWnd, hdcScreen);
//
//	// 转换为 OpenCV Mat（BGR 格式）
//	cv::Mat img(height, width, CV_8UC3, buffer.data());
//	return img.clone(); // 复制一份
//}

// ---------- 鼠标模拟辅助函数 ----------
void AStockDataGrabberDlg::MouseDown(int x, int y)
{
	SetCursorPos(x, y);
	Sleep(30);
	INPUT input = { 0 };
	input.type = INPUT_MOUSE;
	input.mi.dwFlags = MOUSEEVENTF_LEFTDOWN;
	SendInput(1, &input, sizeof(INPUT));
}

void AStockDataGrabberDlg::MouseUp()
{
	INPUT input = { 0 };
	input.type = INPUT_MOUSE;
	input.mi.dwFlags = MOUSEEVENTF_LEFTUP;
	SendInput(1, &input, sizeof(INPUT));
}

void AStockDataGrabberDlg::DragWithTrajectory(int startX, int startY, int endX, int endY)
{
	int steps = 30 + rand() % 20;
	for (int i = 0; i <= steps; ++i) {
		double t = (double)i / steps;
		int x = (int)(startX + (endX - startX) * t + (rand() % 10 - 5));
		int y = (int)(startY + (endY - startY) * t + (rand() % 6 - 3));
		SetCursorPos(x, y);
		Sleep(5 + rand() % 10);
	}
}

// ---------- 核心验证码处理函数 ----------
void AStockDataGrabberDlg::HandleCaptcha()
{
	if (!m_webview) return;

	//先停一停，让可能的验证失败的红色提示小时
	Sleep(2000);

	// 1. 等待验证码弹窗渲染
	m_stCon.SetFocus();
	CRect rect;
	m_stCon.GetClientRect(&rect);
	m_webviewController->put_Bounds(rect);
	m_webviewController->NotifyParentWindowPositionChanged();  // 可选

	Sleep(1500);

	// 2. 第一次截图，定位滑块（此时缺口未显示）
	cv::Mat fullScreen = CaptureWebView2ToMatSync(m_webview);
	if (fullScreen.empty()) {
		TRACE("截图失败\n");
		ResetCaptchaState();
		return;
	}
	cv::imwrite("debug_fullscreen.png", fullScreen);

	cv::Point sliderCenter;
	if (!FindSliderByTemplate(fullScreen, sliderCenter, 0.75)) {
		TRACE("未找到滑块模板\n");
		ResetCaptchaState();
		return;
	}

	// 3. 计算滑块屏幕坐标
	CRect webViewRect;
	m_stCon.GetWindowRect(&webViewRect);
	int screenX = webViewRect.left + sliderCenter.x;
	int screenY = webViewRect.top + sliderCenter.y;

	// 4. 模拟鼠标按下（不释放）
	MouseDown(screenX, screenY);

	// 5. 等待缺口出现（典型 300~500ms）
	Sleep(400);

	// 6. 第二次截图（此时缺口已显示）
	cv::Mat fullScreenWithGap = CaptureWebView2ToMatSync(m_webview);
	if (fullScreenWithGap.empty()) {
		TRACE("第二次截图失败\n");
		MouseUp();
		ResetCaptchaState();
		return;
	}

	cv::imwrite("debug_fullscreen_with_gap.png", fullScreenWithGap);

	// 7. 根据偏移推算背景区域
	int bgX = sliderCenter.x + SLIDER_TO_BG_OFFSET_X;
	int bgY = sliderCenter.y + SLIDER_TO_BG_OFFSET_Y;
	if (bgX < 0 || bgY < 0 || bgX + BG_WIDTH > fullScreenWithGap.cols || bgY + BG_HEIGHT > fullScreenWithGap.rows) {
		TRACE("背景区域越界\n");
		MouseUp();
		ResetCaptchaState();
		return;
	}
	cv::Rect bgRoi(bgX, bgY, BG_WIDTH, BG_HEIGHT);
	cv::Mat bgImage = fullScreenWithGap(bgRoi).clone();

	cv::imwrite("debug_bgImage.png", bgImage);

	// 8. 定位缺口
	int gapXInBg = LocateGapPosition(bgImage);
	if (gapXInBg < 0) {
		TRACE("定位缺口失败\n");
		MouseUp();
		ResetCaptchaState();
		return;
	}

	TRACE("gapXInBg = %d\n", gapXInBg);

	// 9. 计算目标屏幕 X 坐标（缺口左边缘）
	int gapScreenX = webViewRect.left + (bgX + gapXInBg);

	// 10. 计算拖动距离
	int dragDistance = gapScreenX - screenX;

	// 11. 执行拖动（保持按下状态）
	int targetScreenX = screenX + dragDistance;
	DragWithTrajectory(screenX, screenY, targetScreenX, screenY + (rand() % 6 - 3));

	// 12. 释放鼠标
	MouseUp();

	// 13. 等待验证结果
	Sleep(2000);

	// 14. 发送成功消息
	::PostMessage(GetSafeHwnd(), WM_CAPTCHA_SOLVED, 0, 0);
}

void AStockDataGrabberDlg::ResetCaptchaState()
{
	m_bNeedCaptchaSolver = FALSE;
	m_bCaptchaSolving = FALSE;
	MouseUp(); // 确保鼠标释放
}

void AStockDataGrabberDlg::ContinueCollection()
{
	// 从当前位置继续采集
	if (m_nStockIndex < (int)m_listCurStock.size()) {
		GetStockData();
	}
	else {
		m_nStockIndex = 0;
		m_nPage++;
		GetStockList();
	}
}