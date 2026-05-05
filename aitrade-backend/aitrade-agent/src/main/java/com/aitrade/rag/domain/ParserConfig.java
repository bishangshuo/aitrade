package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ParserConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long autoKeywords;
    private Long autoQuestions;
    private Long chunkTokenNum;
    private String delimiter;
    private GraphRag graphrag;
    private Boolean html4excel;
    private String layoutRecognize;
    private Raptor raptor;
    private Long topnTags;
}