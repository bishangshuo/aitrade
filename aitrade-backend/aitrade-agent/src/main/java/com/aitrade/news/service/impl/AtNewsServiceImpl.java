package com.aitrade.news.service.impl;

import com.aitrade.news.domain.AtNews;
import com.aitrade.news.mapper.AtNewsMapper;
import com.aitrade.news.service.IAtNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtNewsServiceImpl implements IAtNewsService {
    @Autowired
    private AtNewsMapper atNewsMapper;
    @Override
    public List<AtNews> selectAtNews(AtNews atNews) {
        return atNewsMapper.selectAtNews(atNews);
    }

    @Override
    public AtNews selectAtNewsById(Long id) {
        return atNewsMapper.selectAtNewsById( id );
    }

    @Override
    public int insertAtNews(AtNews atNews) {
        return atNewsMapper.insertAtNews( atNews );
    }

    @Override
    public int updateAtNewsById(AtNews atNews) {
        return atNewsMapper.updateAtNewsById(atNews);
    }

    @Override
    public int deleteAtNewsById(Long id) {
        return atNewsMapper.deleteAtNewsById(id);
    }

    @Override
    public int deleteAtNewsByIds(Long[] ids) {
        return atNewsMapper.deleteAtNewsByIds(ids);
    }
}
