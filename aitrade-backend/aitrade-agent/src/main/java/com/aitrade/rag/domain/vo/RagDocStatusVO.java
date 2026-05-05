package com.aitrade.rag.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RagDocStatusVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer chunkCount;
    private String createDate;
    private Long createTime;
    private String createdBy;
    private String id;
    private String knowledgebaseId;
    private String location;
    private String name;
    private String chunkMethod;
    private String processBeginAt;

    //解释使用的时间
    private double processDuration;

    //解析进度
    private double progress;

    //解析进度状态信息
    private String progressMsg;

    //解析状态
    private String run;

    //
    private Integer parseStatus;

    private Long size;
    private String sourceType;
    private String status;
    private String thumbnail;
    private Long tokenCount;
    private String type;
    private String updateDate;
    private Long updateTime;
}
