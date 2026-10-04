package com.aitrade.tickflow.utils;

/**
 * 股票过滤器
 * 过滤掉退市、ST等不稳定股票
 * 过滤规则总结
 * 判断维度	规则	                                    是否必须过滤
 * 名称前缀	包含 ST 或 *ST	                        ✅ 必须
 * 名称后缀	以 退 结尾	                            ✅ 必须
 * 名称前缀	以 退市 开头	                            ✅ 必须
 * 代码前缀	400 或 420 开头	                        ✅ 必须
 * 历史状态	回测时用当日的ST状态，而非最新状态	        ⚠️ 关键
 */
public class StockFilter {

    /**
     * 判断是否为应过滤的风险股票（基于最新名称，历史回测需替换为当日快照名称）
     */
    public static boolean isRisky(String name, String code) {
        if (name == null) {
            return false;
        }
        // 1. ST / *ST 前缀
        if (name.startsWith("ST") || name.startsWith("*ST")) {
            return true;
        }
        // 2. 退市整理期：深交所/北交所 "XX退"，上交所 "退市XX"
        if (name.endsWith("退") || name.startsWith("退市")) {
            return true;
        }
        // 3. 老三板代码：400 / 420 开头
        if (code != null && (code.startsWith("400") || code.startsWith("420"))) {
            return true;
        }
        return false;
    }
}
