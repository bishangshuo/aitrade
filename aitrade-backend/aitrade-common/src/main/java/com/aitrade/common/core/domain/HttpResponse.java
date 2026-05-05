package com.aitrade.common.core.domain;


import org.apache.http.HttpStatus;

public class HttpResponse {
    private int statusCode;
    private String body;

    // 构造函数、getter和setter
    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public boolean isSuccess() {
        return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    // getter方法
    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
