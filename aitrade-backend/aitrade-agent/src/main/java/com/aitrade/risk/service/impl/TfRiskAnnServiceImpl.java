package com.aitrade.risk.service.impl;

import java.util.*;

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
    public List<TfRiskAnn> getRiskAnnFromEastMoneyBackward(String stockCode, Date fromDate, int period, int count) {
        List<TfRiskAnn> result = new ArrayList<>();

        // 計算往前推 period 天的截止日期
        Date toDate = DateUtils.addDays(fromDate, -period);

        // 1. 【快取命中檢查】先從本地資料庫查詢該股票在 [toDate, fromDate] 範圍內的公告
        TfRiskAnn query = new TfRiskAnn();
        query.setStockCode(stockCode);
        query.setToDate(toDate);
        query.setFromDate(fromDate);
        List<TfRiskAnn> dbList = tfRiskAnnMapper.selectTfRiskAnnList(query);
        if(dbList != null && dbList.size() >= count) {
            return dbList.subList(0, count);
        }

        // 2. 【聯網補充】本地數量不足，從東方財富網抓取
        List<TfRiskAnn> fetchedList = fetchFromEastMoney(stockCode, fromDate, period, count);

        // 3. 【自動入庫】將新抓取的公告保存到本地資料庫
        if (fetchedList != null && !fetchedList.isEmpty()) {
            for (TfRiskAnn ann : fetchedList) {
                try {
                    // 插入資料庫（若 art_code 已存在且有唯一約束，會拋出異常並被捕獲忽略）
                    tfRiskAnnMapper.insertTfRiskAnn(ann);
                } catch (Exception e) {
                    // 重複資料自動忽略，不影響流程
                }
            }
        }

        // 4. 【最終返回】再次從資料庫查詢並返回最新完整數據
        dbList = tfRiskAnnMapper.selectTfRiskAnnList(query);
        if (dbList == null || dbList.isEmpty()) {
            return null;
        }
        return dbList.size() > count ? dbList.subList(0, count) : dbList;
    }

    /**
     * 原有的網路爬取邏輯（封裝為私有方法）
     */
    private List<TfRiskAnn> fetchFromEastMoney(String stockCode, Date fromDate, int period, int count) {
        List<TfRiskAnn> result = new ArrayList<>();

        Date toDate = DateUtils.addDays(fromDate, -period);
        int pageIndex = 1;
        final int pageSize = 50;

        while (result.size() < count) {
            List<TfRiskAnn> annList = getAnnList(stockCode, pageIndex, pageSize);
            if (annList == null || annList.isEmpty()) {
                break;
            }

            boolean enough = false;
            boolean skipToDate = false;

            for (TfRiskAnn ann : annList) {
                Date pubTime = ann.getPublishTime();
                if (pubTime == null) {
                    continue;
                }

                // 嚴格過濾：必須在 fromDate 之前或剛好同一時間（避免未來函數）
                // 如果您希望包含 fromDate 當天的公告，用 !pubTime.after(endOfDay(fromDate))
                if (pubTime.after(fromDate)) {
                    continue;
                }

                // 如果發布時間早於 toDate（超過 30 天範圍），則觸發終止標記
                if (pubTime.before(toDate)) {
                    skipToDate = true;
                    break;
                }

                result.add(ann);
                if (result.size() >= count) {
                    enough = true;
                    break;
                }
            }

            if (enough || skipToDate) {
                break;
            }
            pageIndex++;
        }

        if (result.isEmpty()) {
            return null;
        }

        // 獲取公告詳情
        for (TfRiskAnn ann : result) {
            Map<String, String> annContent = getAnnContent(ann.getArtCode());
            if (annContent != null) {
                ann.setContent(annContent.get("content"));
                ann.setFileUrl(annContent.get("attachUrl"));
            }
        }
        return result;
    }

    private List<TfRiskAnn> getAnnList(String stockCode, int pageIndex, int pageSize) {
        List<TfRiskAnn> result = new ArrayList<>();
        String url = String.format("https://np-anotice-stock.eastmoney.com/api/security/ann?sr=-1&page_size=%d&page_index=%d&ann_type=A&client_source=web&stock_list=%s&f_node=3&s_node=0",
                pageSize, pageIndex, stockCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Connection", "Keep-Alive");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/156.0.0.0 Safari/537.36");

        HttpResponse response = HttpUtils.doGet(url, null, headers);
        if(response != null && response.isSuccess()) {
            String body = response.getBody();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            if (dataObject == null) return result;

            JSONArray annList = dataObject.getJSONArray("list");
            if (annList == null) return result;

            for(int i = 0; i < annList.size(); i++) {
                JSONObject annObject = annList.getJSONObject(i);
                JSONArray codes = annObject.getJSONArray("codes");
                if (codes == null || codes.isEmpty()) continue;

                JSONObject codeObject = codes.getJSONObject(0);
                String annType = codeObject.getString("ann_type");
                String stockName = codeObject.getString("stock_name");
                String marketCode = codeObject.getString("market_code");

                String artCode = annObject.getString("art_code");
                String title = annObject.getString("title");
                String sourceType = annObject.getString("source_type");

                // 【修復點】：優先使用真實披露時間 (display_time 或 eiTime)，而不是 notice_date
                String displayTimeStr = annObject.getString("display_time");
                if (displayTimeStr == null || displayTimeStr.isEmpty()) {
                    displayTimeStr = annObject.getString("eiTime");
                }

                Date publishTime = parseEastMoneyTime(displayTimeStr);
                if (publishTime == null) {
                    // 退而求其次使用 notice_date
                    publishTime = DateUtils.parseDate(annObject.getString("notice_date"));
                }

                TfRiskAnn tfRiskAnn = new TfRiskAnn();
                tfRiskAnn.setStockCode(stockCode);
                tfRiskAnn.setStockName(stockName);
                tfRiskAnn.setMarketCode(marketCode);
                tfRiskAnn.setAnnType(annType);
                tfRiskAnn.setSourceType(sourceType);
                tfRiskAnn.setArtCode(artCode);
                tfRiskAnn.setTitle(title);
                tfRiskAnn.setPublishTime(publishTime);

                result.add(tfRiskAnn);
            }
        }
        return result;
    }

    /**
     * 輔助方法：解析東方財富的時間格式（支持 "yyyy-MM-dd HH:mm:ss:SSS" 等）
     */
    private Date parseEastMoneyTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        // 處理常見的東方財富時間格式，例如 "2025-11-27 18:19:24:434"
        try {
            // 如果包含毫秒冒號，可以將最後一個冒號替換為點以便標準解析，或者直接用字串截取
            if (timeStr.length() > 19) {
                // 比如 "2025-11-27 18:19:24:434" -> 截取前 19 位 "2025-11-27 18:19:24"
                return DateUtils.parseDate(timeStr.substring(0, 19));
            }
            return DateUtils.parseDate(timeStr);
        } catch (Exception e) {
            return DateUtils.parseDate(timeStr);
        }
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
