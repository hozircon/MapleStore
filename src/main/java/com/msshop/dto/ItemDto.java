package com.msshop.dto;

import com.msshop.domain.Category;
import com.msshop.domain.Item;
import com.msshop.domain.ItemStatus;
import com.msshop.domain.PriceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemDto {

    private Long id;
    private Integer itemId;
    private String name;
    private Category category;
    private String subCategory;
    private String equipType;
    private String equipSubType;
    private Integer strBonus;
    private Integer dexBonus;
    private Integer intBonus;
    private Integer lukBonus;
    private Integer atkBonus;
    private Integer matkBonus;
    private Integer scrollSlotsRemaining;
    private String otherStats;
    private PriceType priceType;
    private Long priceValue;
    private Integer quantity;
    private String location;
    private String sellerName;
    private String warehouseChar;
    private ItemStatus status;

    public static ItemDto from(Item item) {
        ItemDto dto = new ItemDto();
        dto.id                    = item.getId();
        dto.itemId                = item.getItemId();
        dto.name                  = item.getName();
        dto.category              = item.getCategory();
        dto.subCategory           = item.getSubCategory();
        dto.equipType             = item.getEquipType();
        dto.equipSubType          = item.getEquipSubType();
        dto.strBonus              = item.getStrBonus();
        dto.dexBonus              = item.getDexBonus();
        dto.intBonus              = item.getIntBonus();
        dto.lukBonus              = item.getLukBonus();
        dto.atkBonus              = item.getAtkBonus();
        dto.matkBonus             = item.getMatkBonus();
        dto.scrollSlotsRemaining  = item.getScrollSlotsRemaining();
        dto.otherStats            = item.getOtherStats();
        dto.priceType             = item.getPriceType();
        dto.priceValue            = item.getPriceValue();
        dto.quantity              = item.getQuantity();
        dto.location              = item.getLocation();
        dto.sellerName            = item.getSellerName();
        dto.warehouseChar         = item.getWarehouseChar();
        dto.status                = item.getStatus();
        return dto;
    }

    // Convenience: display 0 instead of null for stat fields
    public int getStrBonusOrZero()             { return strBonus != null ? strBonus : 0; }
    public int getDexBonusOrZero()             { return dexBonus != null ? dexBonus : 0; }
    public int getIntBonusOrZero()             { return intBonus != null ? intBonus : 0; }
    public int getLukBonusOrZero()             { return lukBonus != null ? lukBonus : 0; }
    public int getAtkBonusOrZero()             { return atkBonus != null ? atkBonus : 0; }
    public int getMatkBonusOrZero()            { return matkBonus != null ? matkBonus : 0; }
    public int getScrollSlotsRemainingOrZero() { return scrollSlotsRemaining != null ? scrollSlotsRemaining : 0; }
}
