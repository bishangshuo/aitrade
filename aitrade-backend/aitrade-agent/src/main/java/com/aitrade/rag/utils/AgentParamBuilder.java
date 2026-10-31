package com.aitrade.rag.utils;

import java.util.HashMap;
import java.util.Map;

public class AgentParamBuilder {
    public static void makeAgentInputParam(String key, String type, String value, Map<String, Object> inputs) {
        Map<String, Object> input = new HashMap<>();
        input.put("type", type);
        input.put("value", value);
        inputs.put(key, input);
    }
}
