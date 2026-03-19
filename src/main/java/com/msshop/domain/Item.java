package com.msshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "item", indexes = {
    @Index(name = "idx_item_status",            columnList = "status"),
    @Index(name = "idx_item_category",           columnList = "category"),
    @Index(name = "idx_item_price_type_value",   columnList = "price_type, price_value"),
    @Index(name = "idx_item_search_composite",   columnList = "status, category, price_type, price_value")
})
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    // Equipment stat columns (nullable for non-equipment items)
    @Column(name = "str_bonus")
    private Integer strBonus;

    @Column(name = "dex_bonus")
    private Integer dexBonus;

    @Column(name = "int_bonus")
    private Integer intBonus;

    @Column(name = "luk_bonus")
    private Integer lukBonus;

    @Column(name = "atk_bonus")
    private Integer atkBonus;

    @Column(name = "matk_bonus")
    private Integer matkBonus;

    @Column(name = "scroll_slots_remaining")
    private Integer scrollSlotsRemaining;

    /** 裝備種類：防具 / 武器 / 其他（非裝備類為 null） */
    @Column(name = "equip_type", length = 10)
    private String equipType;

    /** 裝備子分類：頭盔 / 單手劍 …（非裝備類為 null） */
    @Column(name = "equip_sub_type", length = 20)
    private String equipSubType;

    /** 其他詰質：自由文字，例：速度+10 HP+200 */
    @Column(name = "other_stats", length = 200)
    private String otherStats;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 10)
    private PriceType priceType;

    @Column(name = "price_value", nullable = false)
    private Long priceValue;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false, length = 50)
    private String location;

    @Column(name = "seller_name", nullable = false, length = 50)
    private String sellerName;

    /** 倉庫角色：僅後台顯示，記錄道具放在哪支角色身上 */
    @Column(name = "warehouse_char", length = 30)
    private String warehouseChar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status = ItemStatus.IN_STOCK;

    @Column(nullable = false)
    private Boolean featured = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
