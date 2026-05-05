package com.aitrade.rag.listener;

public interface ParseProgressListener {
    void onProgress(String docId, int progress);
}
