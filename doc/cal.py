import requests
import csv
import time
import random
from datetime import datetime, timedelta

# 配置信息
BASE_URL = "https://www.szse.cn/api/report/exchange/onepersistenthour/monthList"
OUTPUT_FILE = "szse_data.csv"

# 设置请求头，模拟浏览器访问，防止被反爬拦截
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
    "Referer": "https://www.szse.cn/",
}

def get_month_list(start_year, start_month, end_year, end_month):
    """生成需要抓取的年月列表"""
    months = []
    current = datetime(start_year, start_month, 1)
    end = datetime(end_year, end_month, 1)
    
    while current <= end:
        months.append(current.strftime("%Y-%m"))
        # 增加一个月
        if current.month == 12:
            current = current.replace(year=current.year + 1, month=1)
        else:
            current = current.replace(month=current.month + 1)
    return months

def fetch_data(month_str, session, max_retries=3):
    """获取单个月份的数据，包含重试机制"""
    url = f"{BASE_URL}?month={month_str}&random={random.random()}"
    
    for attempt in range(max_retries):
        try:
            response = session.get(url, headers=HEADERS, timeout=10)
            if response.status_code == 200:
                data = response.json()
                return data.get("data", [])
            else:
                print(f"[警告] {month_str} 状态码异常: {response.status_code}")
        except Exception as e:
            print(f"[错误] {month_str} 请求失败 (尝试 {attempt+1}/{max_retries}): {e}")
        
        if attempt < max_retries - 1:
            time.sleep(2) # 等待2秒后重试
            
    return []

def main():
    # 1. 生成时间范围：2010-01 到 2026-10
    months = get_month_list(2010, 1, 2026, 10)
    print(f"计划抓取月份数量: {len(months)}")

    # 2. 准备 CSV 文件
    # newline='' 是为了防止在 Windows 下写入 CSV 时产生空行
    with open(OUTPUT_FILE, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        # 写入表头
        writer.writerow(["zrxh", "jybz", "jyrq"])

        # 3. 使用 Session 保持连接，提高效率
        session = requests.Session()
        
        success_count = 0
        fail_count = 0

        for month in months:
            print(f"正在抓取: {month} ...")
            data_list = fetch_data(month, session)
            
            if data_list:
                for item in data_list:
                    # 按照要求的字段顺序写入
                    writer.writerow([
                        item.get("zrxh", ""),
                        item.get("jybz", ""),
                        item.get("jyrq", "")
                    ])
                success_count += 1
            else:
                fail_count += 1
                print(f"  -> {month} 未获取到数据")

            # 礼貌爬取，每次请求间隔 0.5 - 1.5 秒，避免给服务器造成过大压力
            time.sleep(random.uniform(4, 8))

    print("-" * 30)
    print(f"抓取完成！")
    print(f"成功月份: {success_count}, 失败/空月份: {fail_count}")
    print(f"文件已保存至: {OUTPUT_FILE}")

if __name__ == "__main__":
    main()