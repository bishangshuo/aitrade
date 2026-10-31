package com.aitrade.rag.enums;

public enum AgentParamType {
    LINE("line", "单行文本"),
    PARAGRAPH("paragraph", "段落文本"),
    NUMBER("integer", "数字"),
    OPTIONS("options", "选项"),
    BOOLEAN("boolean", "布尔值"),
    JSON("object", "JSON对象");

    private String code;
    private String desc;

    AgentParamType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
