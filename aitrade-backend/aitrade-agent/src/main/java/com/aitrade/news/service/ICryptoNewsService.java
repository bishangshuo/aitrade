package com.aitrade.news.service;

import com.aitrade.news.domain.vo.CryptoNews;

import java.util.List;

public interface ICryptoNewsService {
    List<CryptoNews> fetchCoinDesk(int limit);
    List<CryptoNews> fetchCoinTelegraph(int limit);
    List<CryptoNews> fetchBloombergTech(int limit);

    List<CryptoNews> fetchJinse(int limit);
}
