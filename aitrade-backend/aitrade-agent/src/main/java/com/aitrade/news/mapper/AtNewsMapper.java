package com.aitrade.news.mapper;

import com.aitrade.news.domain.AtNews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AtNewsMapper {
    List<AtNews> selectAtNews(AtNews atNews);
    AtNews selectAtNewsById(Long id);

    int insertAtNews(AtNews atNews);

    int updateAtNewsById(AtNews atNews);

    int deleteAtNewsById(Long id);

    int deleteAtNewsByIds(@Param("ids") Long [] ids);
}
