package com.aitrade.rag.component;

import com.aitrade.rag.domain.DataSet;
import com.aitrade.rag.enums.DatasetType;
import com.aitrade.rag.service.IRagApiService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DatasetContainer {

    //线程安全的数据集容器
    private final Map<String, DataSet> datasetMap = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;

    @Autowired
    private IRagApiService ragApiService;

    @PostConstruct
    public void initDataset() {
        try {
            // 初始化数据集容器
            for (DatasetType datasetType : DatasetType.values()) {
                datasetMap.put(datasetType.getKey(), new DataSet());
            }

            // 查询数据集，是否存在数据集名称
            List<DataSet> datasetList = ragApiService.getDatasetList();
            log.info("查询数据集数量: {}", datasetList != null ? datasetList.size() : 0);

            // 遍历数据集容器，如果存在数据集名称，则更新数据集信息，如不存在，则创建数据集
            for (DatasetType datasetType : DatasetType.values()) {
                String datasetKey = datasetType.getKey();

                if (datasetList != null && !datasetList.isEmpty()) {
                    // 使用 Optional 避免 NoSuchElementException
                    Optional<DataSet> existingDataset = datasetList.stream()
                            .filter(item -> item != null && datasetKey.equals(item.getName()))
                            .findFirst();

                    if (existingDataset.isPresent()) {
                        // 更新已存在的数据集信息
                        datasetMap.put(datasetKey, existingDataset.get());
                        log.info("更新数据集: {}", datasetKey);
                    } else {
                        // 创建新的数据集
                        DataSet newDataset = createDataset(datasetType);
                        datasetMap.put(datasetKey, newDataset);
                        log.info("创建新数据集: {}", datasetKey);
                    }
                } else {
                    // 数据集列表为空，创建新的数据集
                    DataSet newDataset = createDataset(datasetType);
                    datasetMap.put(datasetKey, newDataset);
                    log.info("创建新数据集(列表为空): {}", datasetKey);
                }
            }

            log.info("数据集初始化完成");
            log.info("开始同步数据到RAGFlow...");
            initialized = true;
        } catch (Exception e) {
            log.error("初始化数据集失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据集初始化失败", e);
        }
    }

    private DataSet createDataset(DatasetType datasetType) {
        return ragApiService.createDataset(datasetType.getKey(), datasetType.getName());
    }

    public boolean isInitialized() {
        return initialized;
    }

    public DataSet getDataset(String name) {
        return datasetMap.get(name);
    }
}
