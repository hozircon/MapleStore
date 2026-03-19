package com.msshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

/**
 * Catalog entry parsed from MapleStory WZ String data.
 * Stores itemId → name (Chinese) mapping with source category.
 *
 * <p>Implements {@link Persistable} so that Spring Data JPA always calls
 * {@code persist()} instead of {@code merge()} when the ID is manually
 * assigned (no {@code @GeneratedValue}).
 */
@Entity
@Table(name = "game_item", indexes = {
    @Index(name = "idx_game_item_name", columnList = "name")
})
@Getter
@Setter
public class GameItem implements Persistable<Integer> {

    /** 7-digit MapleStory item ID (e.g. 1002357). */
    @Id
    private Integer itemId;

    /** Chinese item name from String.wz (e.g. 黑色勇士戒指). */
    @Column(nullable = false, length = 200)
    private String name;

    /** Source WZ category: Eqp / Consume / Etc / Cash / Ins / Pet */
    @Column(name = "wz_category", length = 30)
    private String wzCategory;

    /** 裝備種類：防具 / 武器 / 其他（非裝備類為 null） */
    @Column(name = "equip_type", length = 10)
    private String equipType;

    /** 裝備子分類：頭盔 / 單手劍 …（非裝備類為 null） */
    @Column(name = "equip_sub_type", length = 20)
    private String equipSubType;

    /** 一般道具子類別：藥水 / 卷軸 / 礦石 …（裝備類為 null） */
    @Column(name = "sub_category", length = 50)
    private String subCategory;

    /**
     * Tracks whether this instance is brand-new (not yet persisted).
     * Marked {@code @Transient} so it is never stored in the database.
     * Reset to {@code false} after persist or load so subsequent saves
     * use merge correctly.
     */
    @Transient
    private boolean isNew;

    /** Required by JPA (reflective instantiation on load). */
    protected GameItem() {
        this.isNew = false;
    }

    public GameItem(Integer itemId, String name, String wzCategory) {
        this.itemId = itemId;
        this.name = name;
        this.wzCategory = wzCategory;
        this.isNew = true;
    }

    public GameItem(Integer itemId, String name, String wzCategory,
                    String equipType, String equipSubType) {
        this(itemId, name, wzCategory);
        this.equipType    = equipType;
        this.equipSubType = equipSubType;
    }

    public GameItem(Integer itemId, String name, String wzCategory, String subCategory,
                    boolean isSubCat) {
        this(itemId, name, wzCategory);
        this.subCategory = subCategory;
    }

    @Override
    public Integer getId() {
        return itemId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    /** Called by Hibernate after INSERT or after loading from DB. */
    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
