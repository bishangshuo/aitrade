package com.aitrade.stock.enums;

public enum BackTestMsg {
    HEART_BEAT("heart_beat", "心跳包");
    private String code;
    private String message;
    BackTestMsg(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
