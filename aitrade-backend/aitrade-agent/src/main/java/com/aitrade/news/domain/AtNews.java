package com.aitrade.news.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 新闻信息表
 create table at_news(
 id bigint not null primary key auto_increment,
 guid varchar(255) COMMENT '新闻源ID',
 title varchar(1024) COMMENT '标题',
 link varchar(1024) COMMENT '原文链接',
 content longtext COMMENT '文章内容，用urlencode编码',
 pub_time datetime COMMENT '新闻发布时间',
 source varchar(255) COMMENT '网站来源',
 author varchar(255) COMMENT '作者',
 is_rag TINYINT(4) default 0 COMMENT '是否已经创建rag文档',
 rag_id varchar(255) COMMENT 'rag文档id',
 status TINYINT default 1,
 create_time datetime,
 update_time datetime
 ) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '新闻表';
 */
@Data
public class AtNews implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String guid;
    private String title;
    private String link;
    private String content;
    private Date pubTime;
    private String source;
    private String author;
    private Integer isRag;
    private String ragId;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    private Date beginPubTime;
    private Date endPubTime;
}
