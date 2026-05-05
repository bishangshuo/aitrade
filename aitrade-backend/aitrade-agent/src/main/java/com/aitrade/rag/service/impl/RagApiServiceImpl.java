package com.aitrade.rag.service.impl;

import com.aitrade.common.core.domain.HttpResponse;
import com.aitrade.common.utils.http.HttpUtils;
import com.aitrade.common.utils.http.StreamHttpClient;
import com.aitrade.rag.domain.DataSet;
import com.aitrade.rag.domain.RagApiResponse;
import com.aitrade.rag.domain.vo.RagDocStatusVO;
import com.aitrade.rag.enums.RagParseStatusType;
import com.aitrade.rag.listener.ParseProgressListener;
import com.aitrade.rag.listener.ParseResultListener;
import com.aitrade.rag.service.IRagApiService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Service
public class RagApiServiceImpl implements IRagApiService {
    private static final Logger log = LoggerFactory.getLogger(RagApiServiceImpl.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @Value("${rag.api.service}")
    private String ragService;

    @Value("${rag.api.key}")
    private String ragApiKey;

    @Autowired
    private StreamHttpClient streamHttpClient;

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 配置命名策略：蛇形命名法转驼峰命名法
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // 忽略未知字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许空字符串转为null
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    private HttpResponse get(String apiName, Map<String, String> params) {
        String url = ragService + apiName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + ragApiKey);
        return HttpUtils.doGet(url, params, headers);
    }

    private HttpResponse post(String apiName, Map<String, String> params) {
        String url = ragService + apiName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Authorization", "Bearer " + ragApiKey);
        String jsonString = JSON.toJSONString(params);
        return HttpUtils.doPostJson(url, jsonString, headers);
    }

    private HttpResponse put(String apiName, Map<String, Object> params) {
        String url = ragService + apiName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + ragApiKey);
        String jsonString = JSON.toJSONString(params);
        return HttpUtils.doPutJson(url, jsonString, headers);
    }

    private HttpResponse postJsonString(String apiName, String jsonString) {
        String url = ragService + apiName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Authorization", "Bearer " + ragApiKey);
        return HttpUtils.doPostJson(url, jsonString, headers);
    }

    private HttpResponse delete(String apiName, Map<String, Object> objectParams) {
        String url = ragService + apiName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + ragApiKey);
        return HttpUtils.doDelete(url, null, objectParams, headers);
    }

    @Override
    public List<DataSet> getDatasetList() {
        List<DataSet> datasetList = null;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("page", "1");
            params.put("page_size", "10000");
            HttpResponse response = get("/api/v1/datasets", params);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    String dataString = jsonObject.getString("data");
                    datasetList = objectMapper.readValue(dataString, new TypeReference<List<DataSet>>() {});
                }
            }
        } catch (Exception e) {
            log.error("获取数据集列表失败", e);
        } finally {
            log.info("获取数据集列表成功，响应内容: " + datasetList);
            return datasetList;
        }
    }

    @Override
    public DataSet createDataset(String key, String name) {
        DataSet dataset = null;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("name", key);
            params.put("description", name);
            params.put("embedding_model",  "text-embedding-v4@Tongyi-Qianwen");
            HttpResponse response = post("/api/v1/datasets", params);
            if (response.isSuccess()) {
                String body = response.getBody();
                RagApiResponse<DataSet> apiResponse =
                        objectMapper.readValue(body, new TypeReference<RagApiResponse<DataSet>>() {});
                dataset = apiResponse.getData();
            }
        } catch (Exception e) {
            log.error("创建数据集失败", e);
        } finally {
            log.info("创建数据集成功，响应内容: " + dataset);
            return dataset;
        }
    }

    /**
     * 上传文本内容
     * @param datasetId
     * @param fileContent
     * @param filename
     * @return
     */
    @Override
    public String uploadStringDocument(
            String datasetId,
            String fileContent,
            String filename
    ) {

        try {

            String url = ragService + "/api/v1/datasets/" + datasetId + "/documents";

            ByteArrayResource fileResource = new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return filename; // 必须
                }
            };

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(ragApiKey);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(form, headers);

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                if(jsonObject.containsKey("data")) {
                    JSONArray arr = jsonObject.getJSONArray("data");
                    if(arr != null && !arr.isEmpty()) {
                        return arr.getJSONObject(0).getString("id");
                    }
                }
            }
        } catch (Exception e) {
            log.error("上传文本内容失败", e);
        }
        return null;
    }

    @Override
    public Integer parseDocument(String datasetId, String docId) {
        try {
            String apiName = "/api/v1/datasets/" + datasetId + "/chunks";
            List<String> docIdList = new ArrayList<>();
            docIdList.add(docId);
            Map<String, List<String>> params = new HashMap<>();
            params.put("document_ids", docIdList);
            String jsonString = JSON.toJSONString(params);
            HttpResponse response = postJsonString(apiName, jsonString);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if (jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    return 0;
                }
            }
        } catch (Exception e) {
            log.error("上传文本内容失败", e);
        }
        return 102;
    }

    @Override
    public String downloadDocument(String datasetId, String docId) {
        try {
            String url = String.format("/api/v1/datasets/%s/documents/%s", datasetId, docId);
            HttpResponse response = get(url, null);
            if (response.isSuccess()) {
                String body = response.getBody();
                if(body != null && !body.isEmpty()) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if (jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(102)) {
                            return null;
                        }
                        return body;
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取数据集列表失败", e);
            return null;
        }
        return null;
    }

    @Override
    public RagDocStatusVO getDocStatus(String datasetId, String docId) {
        try {
            String url = String.format("/api/v1/datasets/%s/documents?id=%s", datasetId, docId);
            HttpResponse response = get(url, null);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    JSONObject dataObj = jsonObject.getJSONObject("data");
                    JSONArray docsArr = dataObj.getJSONArray("docs");
                    if(docsArr != null && !docsArr.isEmpty()) {
                        JSONObject docObj = docsArr.getJSONObject(0);
                        RagDocStatusVO docStatus = objectMapper.convertValue(docObj, RagDocStatusVO.class);
                        if(docStatus != null && docStatus.getRun() != null) {
                            if(docStatus.getRun().equals("DONE")) {
                                docStatus.setParseStatus(RagParseStatusType.PS_SUCCESS.getParseStatus());
                            } else if(docStatus.getRun().equals("FAIL")) {
                                docStatus.setParseStatus(RagParseStatusType.PS_FAIL.getParseStatus());
                            } else if(docStatus.getRun().equals("RUNNING")) {
                                docStatus.setParseStatus(RagParseStatusType.PS_PARSING.getParseStatus());
                            } else if (docStatus.getRun().equals("TIMEOUT")) {
                                docStatus.setParseStatus(RagParseStatusType.PS_TIMEOUT.getParseStatus());
                            } else {
                                docStatus.setParseStatus(RagParseStatusType.PS_UNSTART.getParseStatus());
                            }
                        }
                        return docStatus;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取文档元数据信息失败", e);
        }
        return null;
    }

    @Override
    public int deleteDocument(String datasetId, String docId) {
        try {
            String url = String.format("/api/v1/datasets/%s/documents", datasetId);
            List docIdList = new ArrayList();
            docIdList.add(docId);
            Map<String, Object> params = new HashMap<>();
            params.put("ids", docIdList);
            params.put("delete_all", true);
            HttpResponse response = delete(url, params);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    return 1;
                }
            }
        } catch (Exception e) {
            log.error("删除文档失败", e);
        }
        return 0;
    }

    @Override
    public int updateDocumentMeta(String datasetId, String docId, Map<String, Object> metaFields) {
        try {
            String url = String.format("/api/v1/datasets/%s/documents/%s", datasetId, docId);
            HttpResponse response = put(url, metaFields);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    return 1;
                }
            }
        } catch (Exception e) {
            log.error("更新文档元数据信息失败", e);
        }
        return 0;
    }

    @Override
    public void createDocWithParse(
            String datasetId,
            String filename,
            String content,
            Map<String, Object> metaFields,
            ParseProgressListener progressListener,
            ParseResultListener resultListener
    ) {
        // 上传文档
        String docId = uploadStringDocument(datasetId, content, filename);
        if(docId == null) {
            return;
        }
        // 更新文档元数据
        if(metaFields != null && !metaFields.isEmpty()) {
            updateDocumentMeta(datasetId, docId, metaFields);
        }
        // 发送解析请求
        parseDocument(datasetId, docId);
        // 监听解析进度和结果
        // 开启一个线程，在线程里开启一个定时器，每隔4秒获取一次解析进度和结果
        startPollingStatus(datasetId, docId, progressListener, resultListener);
    }

    private void startPollingStatus(
            String datasetId,
            String docId,
            ParseProgressListener progressListener,
            ParseResultListener resultListener
    ) {
        // 1. 就像 C++ 的指標，我們需要一個容器來存儲這個 Future，
        // 以便在 Runnable 內部能拿到它並執行 "self-cancel"
        AtomicReference<ScheduledFuture<?>> futureHolder = new AtomicReference<>();

        ScheduledFuture<?> scheduledTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                // 獲取狀態
                RagDocStatusVO status = getDocStatus(datasetId, docId);
                if (status == null) return;

                // 正常回傳進度
                if (progressListener != null) {
                    progressListener.onProgress(docId, (int) status.getProgress());
                }

                // 檢查是否結束 (DONE, FAIL, TIMEOUT)
                if (isFinished(status.getRun())) {
                    // 執行最終回調
                    handleFinalResult(status, resultListener, docId);

                    // 【核心：資源釋放】
                    // 拿到自己的控制權，然後把自己從線程池的調度隊列中移除
                    ScheduledFuture<?> self = futureHolder.get();
                    if (self != null) {
                        self.cancel(false);
                        log.info("文檔 {} 解析輪詢任務已安全關閉", docId);
                    }
                }
            } catch (Exception e) {
                log.error("輪詢過程中發生異常", e);
                // 即使出錯也建議關閉，避免死循環請求 API
                Optional.ofNullable(futureHolder.get()).ifPresent(f -> f.cancel(false));
            }
        }, 0, 4, TimeUnit.SECONDS);

        // 2. 將產生的控制句柄存入 Holder
        futureHolder.set(scheduledTask);
    }

    // 輔助方法：判斷是否結束
    private boolean isFinished(String runStatus) {
        return "DONE".equals(runStatus) || "FAIL".equals(runStatus) || "TIMEOUT".equals(runStatus);
    }

    // 輔助方法：處理結果
    private void handleFinalResult(RagDocStatusVO status, ParseResultListener listener, String docId) {
        if (listener == null) return;
        if ("DONE".equals(status.getRun())) {
            listener.onSucess(docId);
        } else {
            listener.onFailure("解析終止，原因：" + status.getRun());
        }
    }

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
    @Override
    public String createAssistant(String name, List<String> datasetIds, Map<String, Object> llmConfig, Map<String, Object> sysPrompt) {
        String chatId = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            params.put("dataset_ids", datasetIds);
            if(llmConfig != null && !llmConfig.isEmpty()) {
                params.put("llm", llmConfig);
            }
            if(sysPrompt != null && !sysPrompt.isEmpty()) {
                params.put("prompt", sysPrompt);
            }
            String paramString = objectMapper.writeValueAsString(params);
            HttpResponse response = postJsonString("/api/v1/chats", paramString);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    if(dataObject != null && dataObject.containsKey("id")) {
                        chatId = dataObject.getString("id");
                    }
                }
            }
        } catch (Exception e) {
            log.error("创建助手失败", e);
        } finally {
            log.info("创建助手成功，助手chatId: " + chatId);
            return chatId;
        }
    }

    /**
     * 创建与助理的会话session
     * @param chatId：助理ID:chat_id，由createAssistant返回
     * @param name
     * @return 会话ID:session_id
     */
    @Override
    public String createSession(String chatId, String name) {
        String sessionId = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            String paramString = objectMapper.writeValueAsString(params);
            String apiName = String.format("/api/v1/chats/%s/sessions", chatId);
            HttpResponse response = postJsonString(apiName, paramString);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    if(dataObject != null && dataObject.containsKey("id")) {
                        sessionId = dataObject.getString("id");
                    }
                }
            }
        } catch (Exception e) {
            log.error("创建会话失败", e);
        } finally {
            log.info("创建会话成功，会话sessionId: " + sessionId);
            return sessionId;
        }
    }

    /**
     * 与助理会话
     * @param chatId：助理ID:chat_id，由createAssistant返回
     * @param sessionId
     * @param metaFields
     * @return 回复
     */
    @Override
    public String converse(String chatId, String sessionId, String prompt, Map<String, Object> metaFields) {
        String responseBody = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("question", prompt);
            params.put("session_id", sessionId);
            if(metaFields != null && !metaFields.isEmpty()) {
                params.put("metadata_condition", metaFields);
            }
            String paramString = objectMapper.writeValueAsString(params);
            String apiName = String.format("/api/v1/chats/%s/completions", chatId);
            HttpResponse response = postJsonString(apiName, paramString);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    if(dataObject != null && dataObject.containsKey("answer")) {
                        responseBody = dataObject.getString("answer");
                    }
                }
            }
        } catch (Exception e) {
            log.error("提问失败:{}", e);
        } finally {
            log.info("提问失败，返回内容: {}", responseBody);
            return responseBody;
        }
    }

    private void converseStream(
            String chatId,
            String sessionId,
            String prompt,
            Map<String, Object> metaFields,
            Function<String, Boolean> resultHandler,
            Runnable onError,
            Runnable onComplete
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("question", prompt);
            params.put("stream", true);
            params.put("session_id", sessionId);
            if(metaFields != null && !metaFields.isEmpty()) {
                params.put("metadata_condition", metaFields);
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + ragApiKey);

            String jsonString = objectMapper.writeValueAsString(params);
            String apiName = String.format("/api/v1/chats/%s/completions", chatId);
            String url = ragService + apiName;
            streamHttpClient.executeStreamPost(url, headers, jsonString, resultHandler, onComplete);
        } catch (Exception e) {
            log.error("流式提问失败 {}", e);
            if(onError != null) {
                onError.run();
            }
        }
    }

    /**
     * 删除会话
     * @param chatId
     * @param sessionIds
     * @return
     */
    @Override
    public int deleteSession(String chatId, List<String> sessionIds) {
        try {
            String url = String.format("/api/v1/chats/%s/sessions", chatId);
            Map<String, Object> params = new HashMap<>();
            params.put("ids", sessionIds);
            HttpResponse response = delete(url, params);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    return 1;
                }
            }
        } catch (Exception e) {
            log.error("删除文档失败", e);
        }
        return 0;
    }

    /**
     * 删除助理
     * @param chatIds
     * @return
     */
    @Override
    public int deleteAssistant(List<String> chatIds) {
        try {
            String url = String.format("/api/v1/chats");
            Map<String, Object> params = new HashMap<>();
            params.put("ids", chatIds);
            HttpResponse response = delete(url, params);
            if (response.isSuccess()) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                if(jsonObject != null && jsonObject.containsKey("code") && jsonObject.getInteger("code").equals(0)) {
                    return 1;
                }
            }
        } catch (Exception e) {
            log.error("删除文档失败", e);
        }
        return 0;
    }

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
    @Override
    public String chat(
            String prompt,
            Map<String, Object> metaFields,
            List<String> datasetIds,
            Map<String, Object> llmConfig,
            Map<String, Object> sysPrompt
    ) {
        //生成随机uuid作为助理名
        String assistantName = UUID.randomUUID().toString();
        //创建助理
        String chatId = createAssistant(assistantName, datasetIds, llmConfig, sysPrompt);
        if(chatId == null || chatId.isEmpty()) {
            log.error("创建助理失败");
            throw new RuntimeException("创建助理失败");
        }
        //创建会话
        String sessoinName = UUID.randomUUID().toString();
        String sessionId = createSession(chatId, sessoinName);
        if(sessionId == null || sessionId.isEmpty()) {
            log.error("创建会话失败");
            throw new RuntimeException("创建会话失败");
        }
        //发起提问会话
        String response = converse(chatId, sessionId, prompt, metaFields);
        if(response == null || response.isEmpty()) {
            log.error("返回为空，提问失败");
        }
        //删除会话
        deleteSession(chatId, Collections.singletonList(sessionId));
        //删除助理
        deleteAssistant(Collections.singletonList(chatId));
        //返回结果
        return sessoinName;
    }

    /**
     * 一次完整会话,流式
     * @param prompt
     * @param metaFields
     * @param datasetIds
     * @param llmConfig
     * @param sysPrompt
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
    @Override
    public void chatStream(
            String prompt,
            Map<String, Object> metaFields,
            List<String> datasetIds,
            Map<String, Object> llmConfig,
            Map<String, Object> sysPrompt,
            Function<String, Boolean> resultHandler,
            Runnable onError,
            Runnable onComplete
    ) {
        //生成随机uuid作为助理名
        String assistantName = UUID.randomUUID().toString();
        //创建助理
        String chatId = createAssistant(assistantName, datasetIds, llmConfig, sysPrompt);
        if(chatId == null || chatId.isEmpty()) {
            log.error("创建助理失败");
            throw new RuntimeException("创建助理失败");
        }
        //创建会话
        String sessoinName = UUID.randomUUID().toString();
        String sessionId = createSession(chatId, sessoinName);
        if(sessionId == null || sessionId.isEmpty()) {
            log.error("创建会话失败");
            throw new RuntimeException("创建会话失败");
        }
        //发起提问会话
        converseStream(
                chatId,
                sessionId,
                prompt,
                metaFields,
                ((rawJson) -> {
                    try {
                        return resultHandler.apply(rawJson);
                    } catch (Exception e) {
                        log.error("处理 RAGFlow 数据块失败: {}, error: {}", rawJson, e);
                        return false;
                    }
                }),
                (() -> {
                    log.error("RAG 流式会话错误");
                    try {
                        if (sessionId != null) {
                            deleteSession(chatId, Collections.singletonList(sessionId));
                        }
                        if (chatId != null) {
                            deleteAssistant(Collections.singletonList(chatId));
                        }
                        if (onError != null) {
                            onError.run();
                        }
                    } catch (Exception e) {
                        log.error("RAG 流式会话错误, 且清理临时 RAG 错误", e);
                    }
                }),
                (()-> {
                    log.info("开始清理临时 RAG 资源：chatId={}, sessionId={}", chatId, sessionId);
                    try {
                        // 顺序不重要，反正都要删
                        if (sessionId != null) {
                            deleteSession(chatId, Collections.singletonList(sessionId));
                        }
                        if (chatId != null) {
                            deleteAssistant(Collections.singletonList(chatId));
                        }
                        log.info("RAG 临时资源清理完成");
                        if(onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        // 记录错误，但不要抛出，避免干扰主流程的状态判断
                        log.error("清理 RAG 资源失败，可能需要手动检查: chatId={}, sessionId={}, error: {}", chatId, sessionId, e);
                    }
                }));
    }
}
