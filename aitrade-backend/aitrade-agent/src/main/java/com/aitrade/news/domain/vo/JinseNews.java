package com.aitrade.news.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 金色财经新闻
 * json格式如下：
 {
 "id": 509045,
 "content": "【一交易员持有ASTEROID代币超580天，浮盈从近零飙升至260万美元】金色财经报道，4月19日，据 Lookonchain 监测，一交易员持有 80.17 亿枚 ASTEROID 超过 580 天后，浮盈已从接近零到 260 万美元。 \n此前消息，马斯克同意已故少女“ASTEROID作为SpaceX吉祥物”的心愿，同名Meme币短时拉升。",
 "content_prefix": "一交易员持有ASTEROID代币超580天，浮盈从近零飙升至260万美元",
 "link_name": "原文链接",
 "link": "https://x.com/lookonchain/status/2045684535653015881",
 "grade": 4,
 "sort": "",
 "category": 2,
 "highlight_color": "",
 "images": [],
 "created_at": 1776564609,
 "created_at_zh": "2026-04-19",
 "attribute": "",
 "up_counts": 524,
 "down_counts": 554,
 "zan_status": "",
 "readings": [],
 "extra_type": 0,
 "extra": null,
 "prev": null,
 "next": null,
 "word_blocks": [],
 "is_show_comment": 1,
 "is_forbid_comment": 0,
 "comment_count": 0,
 "analyst_user": null,
 "show_source_name": "",
 "vote_id": 0,

 */
@Data
public class JinseNews implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String content;
    private String content_prefix;
    private String link;
}
