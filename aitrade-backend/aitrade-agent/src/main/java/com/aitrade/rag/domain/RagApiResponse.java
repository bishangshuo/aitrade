package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RagApiResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer code;
    private T data;
}
