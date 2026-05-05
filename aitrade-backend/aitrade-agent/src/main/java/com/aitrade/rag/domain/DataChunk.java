package com.aitrade.rag.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataChunk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String documentId;
    private String datasetId;
    private String content;
    private String docnmKwd;
    private String imageId;
    private List<String> importantKeywords;
    private List<String> positions;
    private Boolean available;
    private BigDecimal createTimeStamp;
}
