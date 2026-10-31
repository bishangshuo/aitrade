package com.aitrade.common.utils;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;

public class JsonStreamDetector {

    private final Deque<Character> stack = new ArrayDeque<>();

    private boolean inString = false;

    private boolean escape = false;

    // 防止开始时 stack 空直接结束
    private boolean started = false;

    public boolean feed(String chunk) {

        if (chunk == null || chunk.isEmpty()) {
            return false;
        }

        for (int i = 0; i < chunk.length(); i++) {

            char c = chunk.charAt(i);

            // 上一个字符是 \
            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            // 字符串状态切换
            if (c == '"') {
                inString = !inString;
                continue;
            }

            // value里的内容直接忽略
            if (inString) {
                continue;
            }

            switch (c) {

                case '{':
                    stack.push('{');
                    started = true;
                    break;

                case '[':
                    stack.push('[');
                    started = true;
                    break;

                case '}':
                    if (stack.isEmpty() || stack.pop() != '{') {
                        return false;
                    }
                    break;

                case ']':
                    if (stack.isEmpty() || stack.pop() != '[') {
                        return false;
                    }
                    break;

                default:
                    break;
            }

            if (started
                    && stack.isEmpty()
                    && !inString
                    && !escape) {

                return true;
            }
        }

        return false;
    }

    public void reset() {
        stack.clear();
        inString = false;
        escape = false;
        started = false;
    }

    public static String getAnswerFromContent(String content) {
        // 1. 判空与异常捕获
        if (content == null || content.isEmpty()) {
            return "";
        }

        JSONObject jsonContent;
        try {
            jsonContent = JSONObject.parseObject(content);
        } catch (Exception e) {
            return "";
        }

        // 2. 卫语句：提前判断并返回，消除嵌套
        if (jsonContent == null || !jsonContent.containsKey("data")) {
            return "";
        }

        Object dataObj = jsonContent.get("data");
        // 3. 处理 Boolean 类型的特殊情况
        if (dataObj instanceof Boolean) {
            return "";
        }

        // 4. 提取最终答案
        if (dataObj instanceof JSONObject) {
            JSONObject jsonData = (JSONObject) dataObj;
            return jsonData.getString("answer");
        }

        return "";
    }

    public static String getMessageFromAgentData(String content) {
        // 1. 判空与异常捕获
        if (content == null || content.isEmpty()) {
            return "";
        }

        JSONObject jsonData;
        try {
            jsonData = JSONObject.parseObject(content);
        } catch (Exception e) {
            return "";
        }

        if (jsonData == null || !jsonData.containsKey("event")) {
            return "";
        }

        String eventName = jsonData.getString("event");
        if(!eventName.equals("message")) {
            return "";
        }

        // 2. 卫语句：提前判断并返回，消除嵌套
        if (jsonData == null || !jsonData.containsKey("data")) {
            return "";
        }

        JSONObject jsonConent = jsonData.getJSONObject("data");
        if(jsonConent == null) {
            return "";
        }
        String contentText = jsonConent.getString("content");

        return contentText == null ? "" : contentText;
    }
}
