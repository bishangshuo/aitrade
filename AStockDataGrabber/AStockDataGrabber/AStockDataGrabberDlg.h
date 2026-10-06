
// MFCApplication1Dlg.h: 头文件
//

#pragma once

#include <WebView2.h>
#include <wrl/client.h>   // 提供 ComPtr
#include <wrl/event.h>    // 提供 Callback 函数
#include <atlconv.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <string>
#include <nlohmann/json.hpp>
#include <vector>
#include <iomanip>
#include <map>
#include <random>
using namespace std;

#include <shlwapi.h>      // 用于 SHCreateMemStream
#include <functional>

#include <chrono>

#pragma comment(lib, "shlwapi.lib")

using namespace Microsoft::WRL;

using Microsoft::WRL::ComPtr;
using Microsoft::WRL::Callback;



typedef struct _F10_PARAM {
	std::string type;
	std::string sty;
} F10_PARAM;

typedef std::map<int, std::map<std::string, F10_PARAM>> F10_PARAM_MAP;

// AStockDataGrabberDlg 对话框
class AStockDataGrabberDlg : public CDialogEx
{
// 构造
public:
	AStockDataGrabberDlg(CWnd* pParent = nullptr);	// 标准构造函数

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_MFCAPPLICATION1_DIALOG };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持

private:
	void RegisterWebViewEvents();
	bool OpenUrl(const std::string& businessId, const CString& url);
	void DoOpenUrl();
	void GetStockList();
	bool StockListEmpty(const nlohmann::json& jsonData);
	void ProcessStockList(const std::string& responseData);
	void GetStockData(); //采集当前股票的数据，报考K线、财报、新闻
	void GetStockKLine(); //采集当前股票的K线数据
	void GetStockBalanceSheet(const std::string& businessId); //采集当前股票的资产负债表数据
	void GetStockIncomeStatement(const std::string& businessId); //采集当前股票的利润表数据
	void GetStockCashFlowStatement(const std::string& businessId); //采集当前股票的现金流量表数据
	void GetStockFinancialReport(const std::string& businessId, int fType);
	void ProcessStockKLine(const std::string& businessId, const std::string& responseData);
	void ProcessBalanceSheet(const std::string& businessId, const std::string& responseData);
	void ProcessIncomeStatement(const std::string& businessId, const std::string& responseData);
	void ProcessCashFlowStatement(const std::string& businessId, const std::string& responseData);
	void SendRabbitMqMessage(const std::string& businessId, const std::string& responseData);

	bool IsGetFinanceDataSuccess(const nlohmann::json& jsonData);

	void StartSpiderTimer();

	void MarkPosition();
	void ReadPosition();

	//验证码相关函数
	//同步执行 JavaScript 获取元素矩形
	CString GetElementRectSync(const CString& selector);
	//同步截图函数
	cv::Mat CaptureWebViewSync();
	//缺口定位算法
	int LocateGapPosition(const cv::Mat& srcImage);
	//模拟拖动
	void SimulateDrag(int startX, int startY, int endX, int endY);
	//完整 HandleCaptcha 函数
	void HandleCaptcha();
	//继续采集
	void ContinueCollection();

	bool FindSliderByTemplate(const cv::Mat& image, cv::Point& center, double threshold);
	void MouseDown(int x, int y);
	void MouseUp();
	void DragWithTrajectory(int startX, int startY, int endX, int endY);
	void ResetCaptchaState();
	//cv::Mat CaptureWebViewGDI();
	// 
	// void AStockDataGrabberDlg::CaptureWebView2ToMat(
	cv::Mat CaptureWebView2ToMatSync(const Microsoft::WRL::ComPtr<ICoreWebView2>& webView2);

	void PrintCurrentStockInfo();
// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnSize(UINT nType, int cx, int cy);
	afx_msg LRESULT OnWebView2Response(WPARAM wParam, LPARAM lParam);
	afx_msg void OnTimer(UINT_PTR nIDEvent);
	afx_msg void OnBnClickedButtonRequest();
	LRESULT AStockDataGrabberDlg::OnCaptchaTrigger(WPARAM wParam, LPARAM lParam);
	LRESULT AStockDataGrabberDlg::OnCaptchaSolved(WPARAM wParam, LPARAM lParam);
	DECLARE_MESSAGE_MAP()
private:
	ComPtr<ICoreWebView2Controller> m_webviewController;
	ComPtr<ICoreWebView2> m_webview;

	CRect m_rectConInit;   // 存储控件初始矩形（客户区坐标）
	int m_nLeft;
	int m_nTop;
	int m_nRightMargin;   // 初始右边距 = 客户区宽度 - 控件.right
	int m_nBottomMargin;  // 初始下边距 = 客户区高度 - 控件.bottom

	int m_nPage{ 1 };

	ComPtr<ICoreWebView2Environment> m_webviewEnv;
	ComPtr<ICoreWebView2Environment2> m_webviewEnv2;  // 新增

	nlohmann::json m_listCurStock; // 存储当前股票列表的 JSON 对象
	int m_nStockIndex{ 0 }; // 当前处理的股票索引

	UINT_PTR m_spiderTimer{ 0 };

	std::string m_strCurrentBusinessId; // 当前处理的股票业务ID
	CString m_strCurrentUrl; // 当前处理的url

	CButton m_btnReq;
	CStatic m_stCon;

	bool m_bNeedCaptchaSolver;      // 是否需要处理验证码
	bool m_bCaptchaSolving;         // 正在处理验证码中
	bool m_bCaptchaSolved;          // 验证码已解决
	int  m_nCaptchaRetryCount;      // 验证码重试次数（防止死循环）

	cv::Mat m_sliderTemplate;	
};
