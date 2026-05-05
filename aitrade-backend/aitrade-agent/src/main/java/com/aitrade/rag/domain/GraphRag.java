package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class GraphRag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> entityTypes;
    private String method;
    private Boolean useGraphrag;
}
