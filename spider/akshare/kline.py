import pandas as pd
from pytdx.hq import TdxHq_API

api = TdxHq_API()
# 连接通达信公网服务器
if api.connect("119.147.212.81", 7709):
    # 300408 在深市，市场代码为 0（沪市为 1）
    # 获取财务流水/F10 详细财务数据
    data = api.get_finance_info(0, "300408")
    print(pd.Series(data))
    api.close()