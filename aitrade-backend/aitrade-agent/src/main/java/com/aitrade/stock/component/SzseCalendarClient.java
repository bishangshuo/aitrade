package com.aitrade.stock.component;

import com.aitrade.common.utils.DateConvertUtils;
import com.aitrade.stock.domain.TaCal;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SzseCalendarClient {

    private static final String URL =
            "http://www.szse.cn/api/report/exchange/onepersistenthour/monthList";

    private final RestTemplate restTemplate;

    public SzseCalendarClient() {
        org.springframework.http.client.SimpleClientHttpRequestFactory f =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(10_000);
        f.setReadTimeout(15_000);
        this.restTemplate = new RestTemplate(f);
    }

    /**
     * 抓取指定月份的全部日历数据（含开市/休市），calDate 已转为 Date。
     */
    public List<TaCal> fetchMonth(YearMonth ym) {
        String month = ym.toString();
        double random = ThreadLocalRandom.current().nextDouble();
        String url = URL + "?month=" + month + "&random=" + random;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set(HttpHeaders.REFERER, "https://www.szse.cn/market/trading-calendar/");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> resp = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode body = resp.getBody();
        if (body == null || !body.has("data") || !body.get("data").isArray()) {
            throw new IllegalStateException("深交所日历接口返回异常, month=" + month);
        }

        List<TaCal> result = new ArrayList<>();
        for (JsonNode node : body.get("data")) {
            String jyrq = node.path("jyrq").asText();
            String jybz = node.path("jybz").asText("0");
            if (jyrq == null || jyrq.isEmpty()) continue;

            LocalDate date = LocalDate.parse(jyrq);
            TaCal cal = new TaCal();
            cal.setCalDate(DateConvertUtils.toDate(date));            // Date
            cal.setIsOpen("1".equals(jybz) ? 1 : 0);
            cal.setWeekDay(date.getDayOfWeek().getValue());            // 周一=1 … 周日=7
            result.add(cal);
        }
        return result;
    }
}