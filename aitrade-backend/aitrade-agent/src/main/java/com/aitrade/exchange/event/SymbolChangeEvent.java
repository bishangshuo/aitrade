package com.aitrade.exchange.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SymbolChangeEvent extends ApplicationEvent {
    public enum ActionType {
        ADD, REMOVE
    }

    private final String symbol;
    private final ActionType action;

    public SymbolChangeEvent(Object source, String symbol, ActionType action) {
        super(source);
        this.symbol = symbol;
        this.action = action;
    }
}
