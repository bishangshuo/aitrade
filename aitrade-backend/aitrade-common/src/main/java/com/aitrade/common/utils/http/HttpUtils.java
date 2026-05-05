package com.aitrade.common.utils.http;


import com.aitrade.common.constant.Constants;
import com.aitrade.common.core.domain.HttpResponse;
import com.aitrade.common.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用http发送方法
 *
 * @author ruoyi
 */
public class HttpUtils
{
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    // 超时时间配置（单位：毫秒）
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url)
    {
        return sendGet(url, StringUtils.EMPTY);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param)
    {
        return sendGet(url, param, Constants.UTF8);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param contentType 编码类型
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param, String contentType)
    {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try
        {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", result);
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendGet ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendGet SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendGet IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception ex)
            {
                log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @param body 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String body)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try
        {
            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 写入请求body
            out = new PrintWriter(conn.getOutputStream());
            out.print(body);
            out.flush();

            // 读取响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", result);
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + body, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=" + body, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendPost IOException, url=" + url + ",param=" + body, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendPost Exception, url=" + url + ",param=" + body, e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                log.error("调用in.close Exception, url=" + url + ",param=" + body, ex);
            }
        }
        return result.toString();
    }

    public static String sendSSLPost(String url, String param)
    {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        try
        {
            log.info("sendSSLPost - {}", urlNameString);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ret = "";
            while ((ret = br.readLine()) != null)
            {
                if (ret != null && !"".equals(ret.trim()))
                {
                    result.append(new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
            }
            log.info("recv - {}", result);
            conn.disconnect();
            br.close();
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param, e);
        }
        return result.toString();
    }

    /**
     * 发送DELETE请求
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param encode 编码类型
     */
    public static String sendDelete(String url, Map<String, String> headers, String encode) {
        BufferedReader in = null;
        try {
            HttpURLConnection connection = getHttpConnect(url, "DELETE", headers, encode);
            if (connection == null) {
                return null;
            }

            log.info("sendDelete - {}", url);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(inputStream, encode));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
            return result.toString();
        } catch (IOException e) {
            log.error("调用HttpUtils.sendDelete IOException, url=" + url, e);
            return null;
        } catch (Exception e) {
            log.error("调用HttpUtils.sendDelete Exception, url=" + url, e);
            return null;
        } finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception ex)
            {
                log.error("调用in.close Exception, url=" + url, ex);
            }
        }
    }

    public static HttpURLConnection getHttpConnect(String url, String method, Map<String, String> headers, String encode) throws IOException {
        if (StringUtils.isEmpty(url)) {
            log.warn("getHttpConnect - url is empty");
            return null;
        }

        if (StringUtils.isEmpty(encode)) {
            encode = Constants.UTF8;
        }

        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(60000); // 60 secs
        connection.setReadTimeout(60000); // 60 secs
        connection.setRequestProperty("Accept-Charset", encode);
        connection.setRequestProperty("contentType", "application/json");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return connection;
    }

    private static class TrustAnyTrustManager implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }


    /**
     * 创建支持 HTTPS 的 HttpClient
     */
    private static CloseableHttpClient createHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 信任所有证书（仅用于测试环境，生产环境应使用正规证书）
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true)
                .build();

        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, hostnameVerifier);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * GET 请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static HttpResponse doGet(String url, Map<String, String> params, Map<String, String> headers) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpResponse result = null;

        try {
            // 创建HttpClient
            httpClient = createHttpClient();

            // 拼接参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
            }

            // 创建HttpGet请求
            HttpGet httpGet = new HttpGet(url);

            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 执行请求
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            // 获取响应实体
            HttpEntity entity = response.getEntity();
            String body = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            result = new HttpResponse(statusCode, body);
        } catch (Exception e) {
            log.error("GET请求出错: {}", e.getMessage(), e);
            result = new HttpResponse(500, e.getMessage()); // 返回错误状态码
        } finally {
            // 关闭连接
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                log.error("关闭连接出错: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * POST 表单请求
     * @param url 请求地址
     * @param params 表单参数
     * @param headers 请求头
     * @return HttpResponse
     */
    public static HttpResponse doPostForm(String url, Map<String, String> params, Map<String, String> headers) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpResponse result = null;

        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头
            if (headers == null) {
                headers = new HashMap<>();
                // 强制设置为 JSON 格式
                headers.put("Content-Type", "application/json; charset=UTF-8");
            }

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            // 将参数转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(params);

            // 设置 JSON 请求体
            StringEntity stringEntity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);

            // 执行请求
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                result = new HttpResponse(statusCode, resultStr);
                return result;
            }
        } catch (Exception e) {
            log.error("POST请求出错: {}", e.getMessage(), e);
        } finally {
            // 关闭连接
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                log.error("关闭连接出错: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * POST JSON 请求
     * @param url 请求地址
     * @param json JSON字符串
     * @param headers 请求头
     * @return 响应内容
     */
    public static HttpResponse doPostJson(String url, String json, Map<String, String> headers) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpResponse result = null;

        try {
            // 创建HttpClient
            httpClient = createHttpClient();

            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost(url);

            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 设置JSON参数
            if (json != null) {
                StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
                httpPost.setEntity(stringEntity);
            }

            // 执行请求
            response = httpClient.execute(httpPost);

            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = new HttpResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(entity, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("POST JSON请求出错: {}", e.getMessage(), e);
        } finally {
            // 关闭连接
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                log.error("关闭连接出错: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 执行 PUT 请求，发送 JSON 数据
     *
     * @param url 目标 URL
     * @param json JSON 格式的请求体字符串
     * @param headers 自定义请求头 Map (Key: 头名称, Value: 头值)
     * @return 自定义的 HttpResponse 对象
     */
    public static HttpResponse doPutJson(String url, String json, Map<String, String> headers) {
        // 创建 HttpClient 实例
        // 注意：在高并发生产环境中，建议将 CloseableHttpClient 定义为静态单例，而不是每次方法调用都 new
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            // 1. 创建 HttpPut 请求对象
            HttpPut putRequest = new HttpPut(url);

            // 2. 设置请求体 (JSON)
            // 显式指定 UTF-8 编码，防止中文乱码
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            putRequest.setEntity(entity);

            // 3. 设置自定义 Headers
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    putRequest.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 4. 执行请求并获取响应
            try (CloseableHttpResponse response = client.execute(putRequest)) {
                // 获取状态码
                int statusCode = response.getStatusLine().getStatusCode();

                // 获取响应体内容
                String responseBody = "";
                if (response.getEntity() != null) {
                    // 使用 EntityUtils 转换流为字符串，指定 UTF-8
                    responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                }

                // 5. 返回封装好的结果对象
                return new HttpResponse(statusCode, responseBody);
            }

        } catch (Exception e) {
            // 发生网络异常或 IO 异常时
            // 这里可以选择抛出 RuntimeException，或者返回一个特定的错误状态对象
            // 为了符合方法签名，这里抛出运行时异常，调用方需捕获
            throw new RuntimeException("HTTP PUT request failed: " + e.getMessage(), e);
        }
    }

    /**
     * DELETE 请求（支持 JSON Body）
     * @param url 请求地址
     * @param params 请求参数（可为 null）
     * @param body 请求体参数（可为 null）
     * @param headers 请求头
     * @return HttpResponse
     */
    public static HttpResponse doDelete(String url, Map<String, String> params, Map<String, Object> body, Map<String, String> headers) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpResponse result = null;

        try {
            httpClient = createHttpClient();

            // 拼接 URL query 参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                String query = URLEncodedUtils.format(pairs, StandardCharsets.UTF_8);
                url += url.contains("?") ? "&" + query : "?" + query;
            }

            // 创建 HttpDelete 支持 Body
            HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);

            // 设置请求头
            if (headers != null) {
                headers.forEach(httpDelete::setHeader);
            }
            // 默认 JSON
            httpDelete.setHeader("Content-Type", "application/json; charset=UTF-8");

            // 设置 JSON Body
            if (body != null && !body.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(body);
                StringEntity stringEntity = new StringEntity(json, StandardCharsets.UTF_8);
                stringEntity.setContentType("application/json");
                httpDelete.setEntity(stringEntity);
            }

            // 执行请求
            response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String respBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;

            result = new HttpResponse(statusCode, respBody);

        } catch (Exception e) {
            log.error("DELETE请求出错: {}", e.getMessage(), e);
            result = new HttpResponse(500, e.getMessage());
        } finally {
            try {
                if (response != null) response.close();
                if (httpClient != null) httpClient.close();
            } catch (IOException e) {
                log.error("关闭连接出错: {}", e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * 自定义 DELETE 支持 Body
     */
    public static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
