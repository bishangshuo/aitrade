package com.aitrade.tickflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    /** 对象 → PGobject(jsonb)，可直接 setObject 到 PreparedStatement */
    public static PGobject toJsonb(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            PGobject pg = new PGobject();
            pg.setType("jsonb");
            pg.setValue(MAPPER.writeValueAsString(obj));
            return pg;
        } catch (Exception e) {
            throw new IllegalStateException("序列化 JSONB 失败", e);
        }
    }

    /** 字符串 → 对象 */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("反序列化 JSONB 失败: " + json, e);
        }
    }
}