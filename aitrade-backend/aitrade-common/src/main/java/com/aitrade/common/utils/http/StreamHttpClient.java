package com.aitrade.common.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class StreamHttpClient {

    private final CloseableHttpClient httpClient;

    public StreamHttpClient() {
        // 建议配置连接池，这里简化为默认创建
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 执行流式 HTTP POST 请求
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param jsonBody 请求体 JSON 字符串
     * @param dataHandler 数据处理回调：每接收到一行 SSE 数据（去掉 "data: " 前缀后的内容），此方法被调用。
     *                    返回 true 表示继续接收，返回 false 表示主动终止流。
     * @throws IOException 网络或 IO 异常
     */
    public void executeStreamPost(
            String url,
            Map<String, String> headers,
            String jsonBody,
            Function<String, Boolean> dataHandler,
            Runnable onComplete
    ) throws IOException {

        HttpPost httpPost = new HttpPost(url);

        // 设置 headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }

        // 设置 Body
        if (jsonBody != null) {
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        }

        CloseableHttpResponse response = null;
        BufferedReader reader = null;

        try {
            log.info("开始执行流式请求: {}", url);
            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new IOException("HTTP 请求失败: " + statusCode + ", Body: " + errorBody);
            }

            InputStream inputStream = response.getEntity().getContent();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            StringBuilder currentDataBuffer = new StringBuilder();
            boolean isProcessingData = false;

            while ((line = reader.readLine()) != null) {
                // 1. 处理空行 (SSE 消息分隔符)
                if (line.trim().isEmpty()) {
                    if (isProcessingData && currentDataBuffer.length() > 0) {
                        String fullJson = currentDataBuffer.toString();
                        // 回调业务处理
                        boolean shouldContinue = dataHandler.apply(fullJson);
                        currentDataBuffer.setLength(0); // 清空缓冲
                        isProcessingData = false;

                        if (!shouldContinue) {
                            log.info("业务层要求停止流式接收");
                            break;
                        }
                    }
                    continue;
                }

                // 2. 处理 data: 行
                if (line.startsWith("data:")) {
                    isProcessingData = true;
                    String content = line.substring(5).trim();
                    // 累加内容 (防止大 JSON 被拆行，虽然 SSE 通常一行一个)
                    currentDataBuffer.append(content);
                } else if (line.startsWith(":")) {
                    // 注释行，忽略
                    continue;
                } else {
                    // 非标准行，如果是紧接在 data: 后面可能是续行，否则可能是噪音
                    if (isProcessingData) {
                        currentDataBuffer.append(line);
                    }
                }
            }

            // 处理末尾可能残留的数据（如果响应结束时没有双换行）
            if (isProcessingData && currentDataBuffer.length() > 0) {
                dataHandler.apply(currentDataBuffer.toString());
            }


        } catch (Exception e) {
            log.error("流式请求过程中发生异常:{}", e);
            // 异常情况下，isNormalFinish 保持 false
            throw e; // 抛出异常让上层感知，或者在这里吞掉异常只通过回调通知
        }
        finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { /* ignore */ }
            }
            if (response != null) {
                try { response.close(); } catch (IOException e) { /* ignore */ }
            }

            if (onComplete != null) {
                try {
                    onComplete.run();
                } catch (Exception e) {
                    log.error("执行流结束回调时出错: {}", e);
                }
            }
        }
    }
}
