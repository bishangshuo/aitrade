package com.aitrade.news.service.impl;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.news.domain.vo.CryptoNews;
import com.aitrade.news.service.ICryptoNewsService;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CryptoNewsServiceImpl implements ICryptoNewsService {

    @Autowired
    private OkHttpClient httpClient;

    @Override
    public List<CryptoNews> fetchCoinDesk(int limit) {
        return fetchRssNews("https://www.coindesk.com/arc/outboundfeeds/rss/", "coindesk", limit);
    }

    @Override
    public List<CryptoNews> fetchCoinTelegraph(int limit) {
        return fetchRssNews("https://cointelegraph.com/rss", "cointelegraph", limit);
    }

    @Override
    public List<CryptoNews> fetchBloombergTech(int limit) {
        return fetchRssNews("https://feeds.bloomberg.com/technology/news.rss", "bloomberg", limit);
    }

    @Override
    public List<CryptoNews> fetchJinse(int limit) {
        String url = String.format("https://api.jinse2.com/noah/v2/lives?limit=%d&reading=false&source=web&flag=down&category=0", limit);
        return fetchJinseJsonNews(url, "jinse");
    }

    public List<CryptoNews> fetchRssNews(String rssUrl, String sourceName, int limit) {
        List<CryptoNews> newsList = new ArrayList<>();

        Request request = new Request.Builder()
                .url(rssUrl)
                // 这里不需要再手动加 User-Agent！拦截器会自动加上
                // 如果你想额外加其他头，可以在这里加
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful() || response.body() == null) {
                log.error("Failed to fetch RSS from {}: HTTP {}", sourceName, response.code());
                return newsList;
            }

            try (InputStream is = response.body().byteStream();
                 XmlReader reader = new XmlReader(is)) {

                SyndFeed feed = new SyndFeedInput().build(reader);

                for (SyndEntry entry : feed.getEntries()) {
                    if (newsList.size() >= limit) break;

                    CryptoNews news = convertEntryToNews(entry, sourceName);
                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching RSS from {}", sourceName, e);
        }

        enrichFullContent(newsList);   // 补全详情页正文
        return newsList;
    }

    /**
     * SyndEntry → CryptoNews（兼容三种来源）
     */
    private CryptoNews convertEntryToNews(SyndEntry entry, String source) {
        CryptoNews news = new CryptoNews();
        news.setSource(source);
        news.setTitle(entry.getTitle());
        news.setLink(entry.getLink());
        news.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : null);
        news.setGuid(entry.getUri() != null ? entry.getUri() : entry.getLink());
        news.setAuthor(entry.getAuthor());

        // 发布时间
        if (entry.getPublishedDate() != null) {
            news.setPubDate(entry.getPublishedDate());
        }

        // 图片提取（兼容 media:content 和 enclosure）
        if (!entry.getEnclosures().isEmpty()) {
            news.setImageUrl(entry.getEnclosures().get(0).getUrl());
        } else {
            entry.getForeignMarkup().forEach(el -> {
                if ("content".equals(el.getName()) && "media".equals(el.getNamespacePrefix())) {
                    String url = el.getAttributeValue("url");
                    if (url != null && news.getImageUrl() == null) {
                        news.setImageUrl(url);
                    }
                }
            });
        }

        // 分类
        if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
            news.setCategories(entry.getCategories().stream()
                    .map(c -> c.getName())
                    .toList());
        }

        return news;
    }

    /**
     * 为所有新闻补全详情页完整正文（带来源适配）
     */
    private void enrichFullContent(List<CryptoNews> newsList) {
        for (CryptoNews news : newsList) {
            if (news.getLink() == null) continue;

            try {
                String fullContent = fetchArticleFullContent(news.getLink(), news.getSource());
                news.setFullContent(fullContent);

                // 礼貌延迟，降低被反爬风险
                Thread.sleep(700 + (long) (Math.random() * 900));
            } catch (Exception e) {
                log.warn("Failed to fetch full content: {} - {}", news.getSource(), news.getLink(), e);
                news.setFullContent(null);
            }
        }
    }

    /**
     * 根据不同来源使用不同的 Jsoup 选择器抓取正文
     */
    private String fetchArticleFullContent(String articleUrl, String source) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            Element contentElement = null;

            if ("cointelegraph".equalsIgnoreCase(source)) {
                // 1. 最精准的主正文容器
                contentElement = doc.selectFirst("div[data-testid='html-renderer-container']");

                if (contentElement == null) {
                    contentElement = doc.selectFirst("div.ct-prose");
                }
            }
            else if ("coindesk".equalsIgnoreCase(source)) {
                contentElement = findFirstNonEmpty(doc, "div.document-body");
            }
            else if ("bloomberg".equalsIgnoreCase(source)) {
                contentElement = findFirstNonEmpty(doc,
                        "div.body-copy", "div[data-test-id='article-body']", "article");
            }
            else {
                contentElement = doc.selectFirst("article");
            }

            if (contentElement == null || contentElement.text().trim().isEmpty()) {
                return "";
            }

            // ====================== 强力清理（关键优化）======================
            Element cleaned = contentElement.clone();   // 复制一份，避免修改原文档

            // 1. 移除所有已知的无关模块
            cleaned.select("div[data-testid='post-read-more']").remove();           // Read more
            cleaned.select("div[data-ct-widget]").remove();                         // 所有 widget（广告、订阅等）
            cleaned.select("div[class*='joinUsBlock']").remove();                   // 社交订阅块
            cleaned.select("div.my-6.border-b").remove();                           // 免责声明
            cleaned.select("div[data-testid='latest-disclaimer']").remove();        // 免责声明
            cleaned.select("template").remove();                                    // 订阅模板
            cleaned.select("div.mb-6.mt-4").remove();                               // 反应按钮区

            // 2. 移除所有 "Related:" 和 "Magazine:" 开头的段落（最污染的部分）
            cleaned.select("p").forEach(p -> {
                String text = p.text().trim();
                if (text.startsWith("Related:") ||
                        text.startsWith("Magazine:") ||
                        text.contains("Related:") ||
                        text.contains("as Cointelegraph reported")) {
                    p.remove();
                }
            });

            // 3. 移除包含相关文章链接的段落
            cleaned.select("p strong:contains(Related)").remove();
            cleaned.select("p strong:contains(Magazine)").remove();

            // 4. 清理空标签和多余换行
            cleaned.select("p:empty, div:empty").remove();

            // 返回清理后的干净 HTML（适合保留少量格式）
            String cleanHtml = cleaned.html().trim();

            // 如果你希望给 RAG 提供【纯文本】（强烈推荐），使用下面这行代替：
            // String cleanText = cleaned.text().trim();   // 纯文本，去掉所有HTML标签

            return cleanHtml;     // ← 当前返回干净HTML
            // return cleaned.text().trim();   // ← 改成这行就是纯文本

        } catch (Exception e) {
            log.error("Jsoup failed for {}: {}", source, articleUrl, e);
            return "";
        }
    }

    private Element findFirstNonEmpty(Document doc, String... selectors) {
        for (String selector : selectors) {
            Element el = doc.selectFirst(selector);
            if (el != null && !el.text().trim().isEmpty()) {
                return el;
            }
        }
        return null;
    }

    private List<CryptoNews> fetchJinseJsonNews(String url, String sourceName) {

        List<CryptoNews> newsList = new ArrayList<>();

        Request request = new Request.Builder()
                .url(url)
                // 这里不需要再手动加 User-Agent！拦截器会自动加上
                // 如果你想额外加其他头，可以在这里加
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful() || response.body() == null) {
                log.error("Failed to fetch RSS from {}: HTTP {}", sourceName, response.code());
                return newsList;
            }
            String jsonStr = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject jsonObject = JSONObject.parse(jsonStr); // objectMapper.readValue(jsonStr, new TypeReference<JSONObject>() {});
            if(jsonObject == null) {
                return newsList;
            }
            if(!jsonObject.containsKey("list")){
                return newsList;
            }
            JSONArray listDate = jsonObject.getJSONArray("list");
            if(listDate == null || listDate.isEmpty()) {
                return newsList;
            }
            for(int i = 0; i < listDate.size(); i++) {
                JSONObject itemDate = listDate.getJSONObject(i);
                JSONArray lives = itemDate.getJSONArray("lives");
                if(lives != null && !lives.isEmpty()) {
                    for (int j = 0; j < lives.size(); j++) {
                        CryptoNews cryptoNews = new CryptoNews();
                        JSONObject live = lives.getJSONObject(j);
                        cryptoNews.setSource(sourceName);
                        cryptoNews.setGuid(live.getString("id"));
                        cryptoNews.setTitle(live.getString("content_prefix"));
                        cryptoNews.setFullContent(live.getString("content"));
                        cryptoNews.setLink(live.getString("link"));
                        Long createdAt = live.getLong("created_at");
                        Date pubDate = DateUtils.fromTimestamp(createdAt);
                        cryptoNews.setPubDate(pubDate);
                        newsList.add(cryptoNews);
                    }
                }
            }


        } catch (Exception e) {
            log.error("Failed to fetch RSS from {}", sourceName, e);
            return newsList;
        }

        return newsList;
    }
}
