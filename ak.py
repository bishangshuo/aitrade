import requests
import pandas as pd
import time

def fetch_kline_safe(stock_code, klt=101, fqt=1, beg=0, end=20500101):
    """
    安全获取K线，解决第二次请求连接中断的问题
    """
    # 1. 构造证券ID
    if stock_code[0] in ('0', '3'):
        secid = f'0.{stock_code}'
    else:
        secid = f'1.{stock_code}'  # 6开头或5开头

    # 2. 参数构造
    params = {
        'fields1': 'f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13',
        'fields2': 'f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61',
        'beg': str(beg),
        'end': str(end),
        'ut': 'fa5fd1943c7b386f172d6893dbfba10b',
        'rtntype': '6',
        'secid': secid,
        'klt': str(klt),
        'fqt': str(fqt),
    }
    base_url = 'https://push2his.eastmoney.com/api/qt/stock/kline/get'

    # 3. 【关键步骤】完整模拟 Chrome 浏览器的请求头
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        'Connection': 'close',          # 【核心】强制短连接，用完即关，避免服务端重置
        'Referer': 'https://quote.eastmoney.com/',  # 【核心】伪装来源，绝大多数金融接口必带
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'same-site',
        'Cache-Control': 'max-age=0',
    }

    try:
        # 使用 stream=False 确保立即下载完所有数据，不持有连接
        resp = requests.get(base_url, params=params, headers=headers, timeout=10)
        resp.raise_for_status()
        data = resp.json()
    except Exception as e:
        print(f'请求失败: {e}')
        return None

    if data.get('rc') != 0:
        print(f'接口返回异常: {data}')
        return None

    klines = data.get('data', {}).get('klines', [])
    if not klines:
        print('无数据')
        return None

    # 解析数据
    columns = ['日期', '开盘', '收盘', '最高', '最低', '成交量', '成交额', '振幅', '涨跌幅', '涨跌额', '换手率']
    rows = [line.split(',') for line in klines]
    df = pd.DataFrame(rows, columns=columns)
    for col in df.columns[1:]:
        df[col] = pd.to_numeric(df[col], errors='coerce')
    df['日期'] = pd.to_datetime(df['日期'])
    return df

# ---------- 连续循环测试（模拟浏览器多次刷新）----------
if __name__ == '__main__':
    for i in range(5):  # 连续请求5次，验证是否还会中断
        print(f'第 {i+1} 次请求...')
        df = fetch_kline_safe('603806')
        if df is not None:
            print(f'获取到 {len(df)} 条数据，最新日期: {df["日期"].iloc[-1]}')
        else:
            print('请求失败')
        time.sleep(0.5)  # 稍微间隔一下，更稳健