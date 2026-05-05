package com.aitrade.rag.listener;

public interface ParseResultListener {
    void onSucess(String docId);
    void onFailure(String error);
}