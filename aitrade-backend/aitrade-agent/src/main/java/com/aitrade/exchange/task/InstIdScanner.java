package com.aitrade.exchange.task;

import com.aitrade.exchange.domain.AtInst;
import com.aitrade.exchange.mapper.AtInstMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class InstIdScanner {

    @Autowired
    private OkHttpClient client;

    @Resource
    private AtInstMapper instIdMapper;

    private final ObjectMapper mapper = new ObjectMapper();
    @Scheduled(fixedDelay = 3600000)
    public void start() {
        String url = "https://www.okx.com/api/v5/market/tickers?instType=SPOT";
        try {
            List<AtInst> instList = fetchInstIds(url);
            if(instList != null && ! instList.isEmpty()) {
                for(AtInst instId: instList) {
                    upsertInst(instId);
                }
            }
        } catch (Exception e) {
            log.error("fetch inst list error:", e.getMessage());
        }
    }

    private void upsertInst(AtInst inst) {
        //只要instId.getInstId() 中以"-USDT"结尾的币种
        String instId = inst.getInstId();
        if(!instId.endsWith("-USDT")) {
            return;
        }
        AtInst old = instIdMapper.findByInstId(instId);
        if(old != null && instId.equals(old.getInstId())) {
            return;
        }
        instIdMapper.insertAtInst(inst);
    }

    private List<AtInst> fetchInstIds(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.warn("响应体为空");
                return Collections.emptyList();
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);

            // 检查API返回码
            String code = root.get("code").asText();
            if (!"0".equals(code)) {
                log.error("API请求失败: code={}, msg={}", code, root.get("msg").asText());
                return Collections.emptyList();
            }

            if (!root.has("data") || root.get("data").size() == 0) {
                log.debug("无数据返回");
                return Collections.emptyList();
            }

            List<AtInst> result = new ArrayList<>();
            JsonNode dataArray = root.get("data");

            for (JsonNode arr : dataArray) {
                try {
                    AtInst instId = new AtInst();
                    instId.setInstId(arr.get("instId").asText());
                    result.add(instId);
                } catch (Exception e) {
                    log.warn("解析K线数据失败: {}", arr, e);
                }
            }

            log.debug("获取到 {} 条K线数据", result.size());
            return result;
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", url, e);
            throw e;
        }
    }
}
