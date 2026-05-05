package com.aitrade.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20000);   // 20秒
        factory.setReadTimeout(30000);      // 30秒

        restTemplate.setRequestFactory(factory);

        // 添加拦截器，强制使用真实浏览器头
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
            request.getHeaders().set("Accept", "application/rss+xml, text/xml, */*");
            request.getHeaders().set("Accept-Language", "en-US,en;q=0.9");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}