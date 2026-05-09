package com.yujianghuai.fund.model.enums;

import java.util.Arrays;

public enum TransactionStatusEnum {

    PENDING("待确认"),
    CONFIRMED("已确认"),
    FAILED("失败"),
    CANCELED("已撤销");

    private final String description;

    TransactionStatusEnum(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public static TransactionStatusEnum of(String code) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("交易状态不支持"));
    }
}
