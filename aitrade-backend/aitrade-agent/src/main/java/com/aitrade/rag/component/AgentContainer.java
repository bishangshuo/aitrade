package com.aitrade.rag.component;

import com.aitrade.rag.domain.AgentInfo;
import com.aitrade.rag.service.IRagApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AgentContainer {

    @Autowired
    private IRagApiService ragApiService;

    /**
     * 智能体容器，key=agentTitle, value=AgentInfo
     */
    private final Map<String, AgentInfo> agentMap = new ConcurrentHashMap<>();

    /**
     * 应用完全启动后加载智能体缓存
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshAgents();
    }

    /**
     * 根据RAGFlow智能体标题获取Agent ID
     * 若缓存中不存在，则尝试刷新缓存
     */
    public String getAgentId(String agentTitle) {
        AgentInfo agentInfo = agentMap.get(agentTitle);
        if (agentInfo == null) {
            refreshAgents(); // 内部已处理异常，不会抛出
            agentInfo = agentMap.get(agentTitle);
        }
        if (agentInfo == null) {
            log.warn("未找到智能体: {}", agentTitle);
            return null;
        }
        return agentInfo.getId();
    }

    /**
     * 刷新智能体缓存（线程安全）
     */
    private synchronized void refreshAgents() {
        try {
            List<AgentInfo> agentList = ragApiService.listAgents();
            if (agentList == null || agentList.isEmpty()) {
                log.warn("未从RAGFlow获取到智能体列表");
                return;
            }

            // 使用临时Map组装有效数据，避免clear后其他线程读取空Map
            Map<String, AgentInfo> newMap = new ConcurrentHashMap<>();
            for (AgentInfo agentInfo : agentList) {
                if (agentInfo != null && agentInfo.getTitle() != null && !agentInfo.getTitle().isEmpty()) {
                    newMap.put(agentInfo.getTitle(), agentInfo);
                } else {
                    log.warn("跳过无效智能体（title为空或对象为null）: {}", agentInfo);
                }
            }

            // 原子替换
            agentMap.clear();
            agentMap.putAll(newMap);
            log.info("智能体缓存加载完成，共{}个有效智能体", newMap.size());
        } catch (Exception e) {
            // 捕获所有异常，避免因外部服务问题影响应用启动
            log.error("刷新智能体缓存失败，将保留原有缓存（若有）", e);
        }
    }
}
