package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 智能体ID
     */
    private String id;
    /**
     * 智能体名称
     */
    private String title;
    /**
     * 智能体描述
     */
    private String description;
}
