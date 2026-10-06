package com.aitrade.stock.enums;

import lombok.Getter;

@Getter
public enum MarketRegimeEnum {
    // 上升系
    STRONG_UPTREND("主升浪（強上升）", "均線完美多頭排列，斜率陡峭，市場動能極強。", "重倉/全倉，積極參與突破與板塊龍頭。"),
    WEAK_UPTREND("震盪攀升（弱上升）", "價格總體向上但走勢反覆，均線多頭但斜率平緩。", "逢低吸納，回踩 20 日均線建倉，切忌高位追漲。"),
    CLIMAX_UPTREND("加速衝頂（高潮衰竭）", "短線暴漲偏離均線過遠（高乖離率），情緒極度亢奮。", "分批逢高止盈，不追高，提防尖頂回落。"),

    // 盤整系
    RANGE_BOUND("箱體震盪", "均線交織纏繞，價格在一定區間內上下翻騰。", "高拋低吸，利用 BOLL / KDJ 在箱體下軌買入。"),
    LOW_CONSOLIDATION("低位築底（蓄勢）", "經過長期下跌後波幅極度收縮，處於歷史相對低位。", "分批潛伏，等待放量突破第一根大陽線右側加倉。"),
    HIGH_CONSOLIDATION("高位橫盤（派發危險）", "累積較大漲幅後高位震盪，量價背離或放量滯漲。", "減倉防守，鎖定利潤，跌破 20 日均線硬止損。"),

    // 下跌系
    WEAK_DOWNTREND("陰跌修復（弱下跌）", "價格受制於短期均線，缺乏主流資金關註，緩慢下行。", "嚴控倉位（< 20%），不輕易抄底。"),
    STRONG_DOWNTREND("主跌浪（強下跌）", "均線空頭發散，斜率向下陡峭，殺跌動能充足。", "嚴格空倉，絕不接飛刀。"),
    REBOUND_DOWNTREND("超跌反彈（左側/超賣）", "短期跌幅過大偏離均線，指標出現嚴重超賣或底背離。", "極輕倉快進快出，反彈至 10日/20日 均線遇阻即止盈。");

    private final String name;
    private final String description;
    private final String strategy;

    MarketRegimeEnum(String name, String description, String strategy) {
        this.name = name;
        this.description = description;
        this.strategy = strategy;
    }
}
