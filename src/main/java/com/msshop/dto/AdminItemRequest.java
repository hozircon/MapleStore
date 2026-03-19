package com.msshop.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AdminItemRequest {

    @NotNull(message = "道具 ID 為必填")
    @Min(value = 1, message = "道具 ID 必須大於 0")
    private Integer itemId;

    @NotBlank(message = "商品名稱為必填")
    @Size(min = 1, max = 100, message = "名稱長度須為 1–100 字元")
    private String name;

    @NotBlank(message = "大類為必填")
    private String category;

    private String subCategory;

    @Min(value = 0, message = "STR 不得為負數")
    private Integer strBonus;

    @Min(value = 0, message = "DEX 不得為負數")
    private Integer dexBonus;

    @Min(value = 0, message = "INT 不得為負數")
    private Integer intBonus;

    @Min(value = 0, message = "LUK 不得為負數")
    private Integer lukBonus;

    @Min(value = 0, message = "ATK 不得為負數")
    private Integer atkBonus;

    @Min(value = 0, message = "MATK 不得為負數")
    private Integer matkBonus;

    @Min(value = 0, message = "剩餘捲次不得為負數")
    private Integer scrollSlotsRemaining;

    @NotBlank(message = "貨幣類型為必填")
    private String priceType;

    @NotNull(message = "標價為必填")
    @Min(value = 0, message = "標價不得為負數")
    private Long priceValue;

    @NotNull(message = "數量為必填")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;

    @NotBlank(message = "遊戲位置為必填")
    @Size(min = 1, max = 50, message = "位置長度須為 1–50 字元")
    private String location;

    @NotBlank(message = "賣家名稱為必填")
    @Size(min = 1, max = 50, message = "賣家名稱長度須為 1–50 字元")
    private String sellerName;
}
