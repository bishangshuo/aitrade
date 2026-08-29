import requests
import json

def fetch_kline_tencent(stock_code="603806"):
    # 腾讯接口：sh=上海，sz=深圳
    market = "sh" if stock_code.startswith(("6", "5")) else "sz"
    url = f"https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param={market}{stock_code},day,,,"
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    
    resp = requests.get(url, headers=headers, timeout=10)
    data = resp.json()
    
    # 解析数据
    key = f"{market}{stock_code}"
    klines = data.get('data', {}).get(key, {}).get('day', [])
    
    if not klines:
        print("未获取到数据")
        return []
    
    # 数据格式: [日期, 开盘, 收盘, 最高, 最低, 成交量, 成交额]
    print(f"✅ 获取到 {len(klines)} 条数据")
    print(f"最新一条: {klines[-1]}")
    return klines

# 随便刷，完全不会断
for i in range(10):
    print(f"\n第 {i+1} 次请求")
    fetch_kline_tencent("603806")