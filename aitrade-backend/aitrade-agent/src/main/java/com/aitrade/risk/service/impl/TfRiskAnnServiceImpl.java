package com.aitrade.risk.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aitrade.common.core.domain.HttpResponse;
import com.aitrade.common.utils.DateUtils;
import com.aitrade.common.utils.http.HttpUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aitrade.risk.mapper.TfRiskAnnMapper;
import com.aitrade.risk.domain.TfRiskAnn;
import com.aitrade.risk.service.ITfRiskAnnService;

/**
 * 风险公告Service业务层处理
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Service
public class TfRiskAnnServiceImpl implements ITfRiskAnnService 
{
    @Autowired
    private TfRiskAnnMapper tfRiskAnnMapper;

    /**
     * 查询风险公告
     * 
     * @param id 风险公告主键
     * @return 风险公告
     */
    @Override
    public TfRiskAnn selectTfRiskAnnById(Long id)
    {
        return tfRiskAnnMapper.selectTfRiskAnnById(id);
    }

    /**
     * 查询风险公告列表
     * 
     * @param tfRiskAnn 风险公告
     * @return 风险公告
     */
    @Override
    public List<TfRiskAnn> selectTfRiskAnnList(TfRiskAnn tfRiskAnn)
    {
        return tfRiskAnnMapper.selectTfRiskAnnList(tfRiskAnn);
    }

    /**
     * 新增风险公告
     * 
     * @param tfRiskAnn 风险公告
     * @return 结果
     */
    @Override
    public int insertTfRiskAnn(TfRiskAnn tfRiskAnn)
    {
        tfRiskAnn.setCreateTime(DateUtils.getNowDate());
        return tfRiskAnnMapper.insertTfRiskAnn(tfRiskAnn);
    }

    /**
     * 修改风险公告
     * 
     * @param tfRiskAnn 风险公告
     * @return 结果
     */
    @Override
    public int updateTfRiskAnn(TfRiskAnn tfRiskAnn)
    {
        return tfRiskAnnMapper.updateTfRiskAnn(tfRiskAnn);
    }

    /**
     * 批量删除风险公告
     * 
     * @param ids 需要删除的风险公告主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskAnnByIds(Long[] ids)
    {
        return tfRiskAnnMapper.deleteTfRiskAnnByIds(ids);
    }

    /**
     * 删除风险公告信息
     * 
     * @param id 风险公告主键
     * @return 结果
     */
    @Override
    public int deleteTfRiskAnnById(Long id)
    {
        return tfRiskAnnMapper.deleteTfRiskAnnById(id);
    }

    @Override
    public List<TfRiskAnn> getRiskAnnFromEastMoney(String stockCode) {
        List<TfRiskAnn> result = new ArrayList<>();
        String url = String.format("https://np-anotice-stock.eastmoney.com/api/security/ann?sr=-1&page_size=50&page_index=2&ann_type=A&client_source=web&stock_list=%s&f_node=0&s_node=0", stockCode);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Connection", "Keep-Alive");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/156.0.0.0 Safari/537.36");
        HttpResponse response = HttpUtils.doGet(url, null, headers);
        if(response != null && response.isSuccess()) {
            String body = response.getBody();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray annList = dataObject.getJSONArray("list");
            for(int i = 0; i < annList.size(); i++) {
                JSONObject annObject = annList.getJSONObject(i);
                JSONArray codes = annObject.getJSONArray("codes");
                JSONObject codeObject = codes.getJSONObject(0);
                String annType = codeObject.getString("ann_type");
                String stockName = codeObject.getString("stock_name");
                String marketCode = codeObject.getString("market_code");

                String artCode = annObject.getString("art_code");
                String title = annObject.getString("title");
                String sourceType = annObject.getString("source_type");
                String noticeDate = annObject.getString("notice_date");

                Map<String, String> contentMap = getAnnContent(artCode);
                String content = contentMap.get("content");
                String attachUrl = contentMap.get("attachUrl");

                TfRiskAnn tfRiskAnn = new TfRiskAnn();
                tfRiskAnn.setStockCode(stockCode);
                tfRiskAnn.setStockName(stockName);
                tfRiskAnn.setMarketCode(marketCode);
                tfRiskAnn.setAnnType(annType);
                tfRiskAnn.setSourceType(sourceType);
                tfRiskAnn.setArtCode(artCode);
                tfRiskAnn.setTitle(title);
                tfRiskAnn.setContent(content);
                tfRiskAnn.setFileUrl(attachUrl);
                tfRiskAnn.setPublishTime(DateUtils.parseDate(noticeDate));
                result.add(tfRiskAnn);
            }
        }
        return result;
    }

    private Map<String, String> getAnnContent(String artCode) {
        //https://np-cnotice-stock.eastmoney.com/api/content/ann?cb=jQuery112302720425009740174_1793114021220&art_code=AN202507231714218828&client_source=web&page_index=1&_=1793114021221
        String hashTag = String.valueOf(System.currentTimeMillis());
        String url = String.format("https://np-cnotice-stock.eastmoney.com/api/content/ann?art_code=%s&client_source=web&page_index=1&_=%s", artCode, hashTag);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Connection", "Keep-Alive");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/156.0.0.0 Safari/537.36");
        HttpResponse response = HttpUtils.doGet(url, null, headers);
        if(response != null && response.isSuccess()) {
            String body = response.getBody();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            String content = dataObject.getString("notice_content");
            String attachUrl = dataObject.getString("attach_url");
            return new HashMap<String, String>() {{
                put("content", content);
                put("attachUrl", attachUrl);
            }};
        }
        return null;
    }
}
