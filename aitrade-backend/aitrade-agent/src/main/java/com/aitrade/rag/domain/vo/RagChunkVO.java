package com.aitrade.rag.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RagChunkVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String documentId;
    private String content;
}
