package com.aitrade.stock.enums;

public enum TaskStatusEnum {
    NOT_START(0, "未开始"),
    ONGOING(1, "进行中"),
    COMPLETE(2, "已完成"),
    EXPIRED(3, "已过期");

    private final int status;
    private final String message;

    TaskStatusEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
