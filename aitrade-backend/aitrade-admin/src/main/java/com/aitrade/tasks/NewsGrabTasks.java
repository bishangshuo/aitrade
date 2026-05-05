package com.aitrade.tasks;

import com.aitrade.common.utils.DateUtils;
import com.aitrade.news.domain.AtNews;
import com.aitrade.news.domain.vo.CryptoNews;
import com.aitrade.news.service.IAtNewsService;
import com.aitrade.news.service.ICryptoNewsService;
import com.aitrade.rag.component.DatasetContainer;
import com.aitrade.rag.domain.DataSet;
import com.aitrade.rag.enums.DatasetType;
import com.aitrade.rag.listener.ParseProgressListener;
import com.aitrade.rag.listener.ParseResultListener;
import com.aitrade.rag.service.IRagApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NewsGrabTasks {
    @Autowired
    private ICryptoNewsService cryptoNewsService;

    @Autowired
    private IAtNewsService atNewsService;

    @Autowired
    private IRagApiService ragApiService;

    @Autowired
    private DatasetContainer datasetContainer;

    //定时任务, 5分钟执行一次
    @Scheduled(fixedDelay = 60000)
    public void grabJinseNews() {
        System.out.println("JinseNewsGrab.grab()");
        List<CryptoNews> list = cryptoNewsService.fetchJinse(20);
        saveNews(list);
    }

    //@Scheduled(fixedDelay = 300000)
    public void grabCoinTelegraphNews() {
        System.out.println("CointelegraphGrab.grab()");
        List<CryptoNews> list = cryptoNewsService.fetchCoinTelegraph(20);
        saveNews(list);
    }

    private void saveNews(List<CryptoNews> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        uploadToRagflow(0, list);
    }

    private AtNews fromCryptoNews(CryptoNews news) {
        if(news == null) {
            return null;
        }

        AtNews atNews = new AtNews();
        atNews.setGuid(news.getGuid());
        atNews.setTitle(news.getTitle());
        atNews.setLink(news.getLink());
        atNews.setContent(news.getFullContent());
        atNews.setPubTime(news.getPubDate());
        atNews.setSource(news.getSource());
        atNews.setAuthor(news.getAuthor());

        return atNews;
    }

    //上传到ragflow，建立rag文档并解析，返回rag文档ID

    /**
     * void createDocWithParse(
     *             String datasetId,
     *             String filename,
     *             String content,
     *             Map<String, Object> metaFields,
     *             ParseProgressListener progressListener,
     *             ParseResultListener resultListener
     *     );
     * @param index
     * @param newsList
     * @return
     */
    private void uploadToRagflow(int index, List<CryptoNews> newsList) {
        if(newsList == null || newsList.isEmpty()) {
            return;
        }
        if(index >= newsList.size()) {
            return;
        }
        CryptoNews cryptoNews = newsList.get(index);

        //如果已经入库，则跳过
        AtNews query = new AtNews();
        query.setGuid(cryptoNews.getGuid());
        List<AtNews> existsAtNews = atNewsService.selectAtNews(query);
        if(existsAtNews != null && !existsAtNews.isEmpty()) {
            int nextIndex = index + 1;
            uploadToRagflow(nextIndex, newsList);
            return;
        }

        AtNews atNews = fromCryptoNews(cryptoNews);
        //插入mysql数据库
        int res = atNewsService.insertAtNews(atNews);
        if(res <= 0) {
            int nextIndex = index + 1;
            uploadToRagflow(nextIndex, newsList);
            return;
        }

        DataSet dataset = datasetContainer.getDataset(DatasetType.NEWS.getKey());
        String datesetId = dataset.getId();
        String fileName = atNews.getTitle() + ".md";
        //fileName去掉空格和特殊字符，以便用于文档的文件名
        fileName = fileName.replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");

        StringBuilder sb = new StringBuilder();
        sb.append("# title:\n").append(atNews.getTitle());
        String pubTimeStr = DateUtils.formatDate(atNews.getPubTime());
        sb.append("\n\n# public time:\n").append(pubTimeStr);
        sb.append("\n\n# author:\n").append(atNews.getAuthor());
        sb.append("\n\n# source:\n").append(atNews.getSource());
        sb.append("\n\n# content:\n").append(atNews.getContent());

        Map<String, Object> metaFields = new HashMap<>();
        metaFields.put("title", atNews.getTitle());
        metaFields.put("pub_time", pubTimeStr);
        metaFields.put("author", atNews.getAuthor());
        metaFields.put("source", atNews.getSource());

        ragApiService.createDocWithParse(
                datesetId,
                fileName,
                sb.toString(),
                metaFields,
                new ParseProgressListener() {
                    @Override
                    public void onProgress(String docId, int progress) {
                        System.out.println("parse progress: docId= " + docId + ", progress=" + progress);
                    }
                },
                new ParseResultListener() {
                    @Override
                    public void onSucess(String docId) {
                        System.out.println("upload and parse success: " + docId);
                        atNews.setIsRag(1);
                        atNews.setRagId(docId);
                        atNewsService.updateAtNewsById(atNews);
                        int nextIndex = index + 1;
                        uploadToRagflow(nextIndex, newsList);
                    }

                    @Override
                    public void onFailure(String error) {
                        System.out.println("upload or parse failure: " + error);
                        int nextIndex = index + 1;
                        uploadToRagflow(nextIndex, newsList);
                    }
                }
        );
    }
}
