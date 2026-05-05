package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DataSet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String avatar;
    private Long chunkCount;
    private String chunkMethod;
    private String createDate;
    private Long createTime;
    private String createdBy;
    private String description;
    private Long documentCount;
    private String embeddingModel;
    private String graphragTaskFinishAt;
    private String graphragTaskId;
    private String language;
    private String mindmapTaskFinishAt;
    private String mindmapTaskId;
    private Long pagerank;
    private ParserConfig parserConfig;
    private String permission;
    private String pipelineId;
    private String raptorTaskFinishAt;
    private String raptorTaskId;
    private BigDecimal similarityThreshold;
    private String status;
    private String tenantId;
    private Long tokenNum;
    private String updateDate;
    private Long updateTime;
    private BigDecimal vectorSimilarityWeight;
}