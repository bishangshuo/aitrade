package com.aitrade.news.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CryptoNews implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String source;          // "coindesk" | "cointelegraph" | "bloomberg"
    private String title;
    private String link;
    private String description;     // RSS 摘要
    private String fullContent;     // 详情页抓取的完整正文 HTML
    private Date pubDate;
    private String author;
    private String guid;
    private String imageUrl;
    private List<String> categories;
}
