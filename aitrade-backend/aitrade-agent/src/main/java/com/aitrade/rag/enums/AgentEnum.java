package com.aitrade.rag.enums;

public enum AgentEnum {
    AGENT_FILTER_RISK_ANNOUNCE("agent_filter_risk_announce", "智能体：风险公告过滤");
    private String title;
    private String description;

    AgentEnum(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
