package com.aitrade.rag.service;

import com.aitrade.rag.domain.DataSet;
import com.aitrade.rag.domain.vo.RagDocStatusVO;
import com.aitrade.rag.listener.ParseProgressListener;
import com.aitrade.rag.listener.ParseResultListener;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IRagApiService {
    /**
     * 获取数据集列表
     * @return
     */
    List<DataSet> getDatasetList();

    /**
     * 创建数据集
     * @param key
     * @param name
     * @return
     */
    DataSet createDataset(String key, String name);

    /**
     * 上传文本文档
     * @param datasetId
     * @param fileContent
     * @param filename
     * @return
     * 返回文档ID
     */
    String uploadStringDocument(
            String datasetId,
            String fileContent,
            String filename
    );

    /**
     * 解析文档
     * @param datasetId
     * @param docId
     * @return
     * 0 成功
     * 102 文档解析失败，docId 不存在
     */
    Integer parseDocument(String datasetId, String docId);

    /**
     * 下载文档
     * @param datasetId
     * @param docId
     * @return
     */
    String downloadDocument(String datasetId, String docId);

    RagDocStatusVO getDocStatus(String datasetId, String docId);

    /**
     * 删除文档
     * @param datasetId
     * @param docId
     * @return
     */
    int deleteDocument(String datasetId, String docId);

    int updateDocumentMeta(String datasetId, String docId, Map<String, Object> metaFields);

    /**
     * 创建文档并解析，异步回调结果和进度
     * @param datasetId
     * @param filename
     * @param content
     * @param metaFields
     * @param progressListener
     * @param resultListener
     */
    void createDocWithParse(
            String datasetId,
            String filename,
            String content,
            Map<String, Object> metaFields,
            ParseProgressListener progressListener,
            ParseResultListener resultListener
    );

    /**
     * 创建助理assistant
     * @param name: 助理名称
     * @param datasetIds: 助理所关联的知识库ID列表
     * @param llmConfig: 模型配置
     *                 如果为null，则使用后台配置的默认模型，否则使用字典配置，格式如下：
     *                  {
     *                     "model_name": "qwen-max",
     *                  }
     *                 详情查看RAGFlow的文档
     * @return 助理ID:chat_id
     */
    String createAssistant(String name, List<String> datasetIds, Map<String, Object> llmConfig, Map<String, Object> sysPrompt);

    /**
     * 创建与助理的会话session
     * @param chatId：助理ID:chat_id，由createAssistant返回
     * @param name
     * @return 会话ID:session_id
     */
    String createSession(String chatId, String name);

    /**
     * 与助理会话
     * @param chatId：助理ID:chat_id，由createAssistant返回
     * @param sessionId
     * @param metaFields
     * @return 回复
     */
    String converse(String chatId, String sessionId, String prompt, Map<String, Object> metaFields);

    /**
     * 删除会话
     * @param chatId
     * @param sessionIds
     * @return
     */
    int deleteSession(String chatId, List<String> sessionIds);

    /**
     * 删除助理
     * @param chatIds
     * @return
     */
    int deleteAssistant(List<String> chatIds);

    /**
     * 一次完整会话
     * @param prompt
     * @param metaFields
     * @param datasetIds
     * @param llmConfig
     *
     * 步骤：
     * 1. 创建助理
     * 2. 创建会话
     * 3. 向会话提问
     * 4. 删除会话
     * 5. 删除助理
     */
    String chat(
            String prompt,
            Map<String, Object> metaFields,
            List<String> datasetIds,
            Map<String, Object> llmConfig,
            Map<String, Object> sysPrompt
    );

    /**
     * 一次完整会话,流式
     * @param prompt
     * @param metaFields
     * @param datasetIds
     * @param llmConfig
     * @param resultHandler:回调
     * @param onError:错误回调
     * @param onComplete:完成回调
     *
     * 步骤：
     * 1. 创建助理
     * 2. 创建会话
     * 3. 向会话提问
     * 4. 删除会话
     * 5. 删除助理
     */
    void chatStream(
            String prompt,
            Map<String, Object> metaFields,
            List<String> datasetIds,
            Map<String, Object> llmConfig,
            Map<String, Object> sysPrompt,
            Function<String, Boolean> resultHandler,
            Runnable onError,
            Runnable onComplete
    );
}
