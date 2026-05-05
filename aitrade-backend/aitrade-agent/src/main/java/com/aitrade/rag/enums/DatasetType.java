package com.aitrade.rag.enums;

public enum DatasetType {
    NEWS("dataset_news", "新闻知识库"),
    PROJECT("dataset_projects", "项目知识库");

    private String key;
    private String name;

    DatasetType(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static String getNameByKey(String key) {
        for (DatasetType type : DatasetType.values()) {
            if (type.getKey().equals(key)) {
                return type.getName();
            }
        }
        return null; // 或者抛异常，视业务需要
    }
}
