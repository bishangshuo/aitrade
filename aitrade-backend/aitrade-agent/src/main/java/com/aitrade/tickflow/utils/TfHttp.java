package com.aitrade.tickflow.utils;

import com.aitrade.common.utils.http.HttpUtils;

import java.util.Map;

public class TfHttp {

    public static String httpGet(String url, Map<String, Object> params) {
        //params转为&连接的表单字符串
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        String paramStr = sb.toString();
        if (paramStr.length() > 0) {
            paramStr = paramStr.substring(0, paramStr.length() - 1);
        }
        return HttpUtils.sendGet(url, paramStr);
    }
}
