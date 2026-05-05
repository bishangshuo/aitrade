package com.aitrade.rag.enums;

public enum RagParseStatusType {
    PS_UNSTART(0, "未开始"),
    PS_PARSING(1, "解析中"),
    PS_SUCCESS(2, "成功"),
    PS_TIMEOUT(3, "超时"),
    PS_FAIL(4, "失败");

    private Integer parseStatus;
    private String info;
    RagParseStatusType(Integer parseStatus, String info) {
        this.parseStatus = parseStatus;
        this.info = info;
    }

    public Integer getParseStatus() {
        return parseStatus;
    }

    public String getInfo() {
        return info;
    }

    public static String getInfoByParseStatus(Integer parseStatus) {
        for (RagParseStatusType type : RagParseStatusType.values()) {
            if (type.getParseStatus().equals(parseStatus)) {
                return type.getInfo();
            }
        }
        return null; // 或者抛异常，视业务需要
    }
}
