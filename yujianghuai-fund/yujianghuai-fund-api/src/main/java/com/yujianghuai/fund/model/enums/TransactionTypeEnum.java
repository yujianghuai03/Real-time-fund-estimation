package com.yujianghuai.fund.model.enums;

import java.util.Arrays;

public enum TransactionTypeEnum {

    BUY("申购"),
    SELL("赎回"),
    SWITCH("转换"),
    DIVIDEND("分红"),
    SIP("定投"),
    ADJUST("调整");

    private final String description;

    TransactionTypeEnum(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public static TransactionTypeEnum of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("交易类型不支持"));
    }
}
