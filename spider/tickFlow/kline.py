import requests

url = "https://free-api.tickflow.org/v1/klines/batch?symbols=000001.SH,588950.SH,605198.SH,600884.SH,600800.S,600967.SH&period=1d&start_time=1632758400000&count=10000"

headers = {"x-api-key": "tk_04247d60a5ac47e78ce933b1e6ec1e6a"}

response = requests.get(url, headers=None)

print(response.text)