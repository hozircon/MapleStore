package com.msshop.domain;

public enum Category {
    EQUIPMENT("裝備"),
    CONSUMABLE("消耗"),
    DECORATIVE("裝飾"),
    OTHER("其他");

    private final String label;

    Category(String label) { this.label = label; }

    public String getLabel() { return label; }
}
