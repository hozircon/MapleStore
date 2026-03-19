package com.msshop.domain;

public enum PriceType {
    MESO("楓幣"),
    CS("CS"),
    WS("WS");

    private final String label;

    PriceType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
