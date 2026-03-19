package com.msshop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequest {

    private String keyword;
    private String category;
    private String subCategory;
    private List<String> priceTypes;

    // Equipment stat threshold filters (null = no minimum)
    private Integer strMin;
    private Integer dexMin;
    private Integer intMin;
    private Integer lukMin;
    private Integer atkMin;
    private Integer matkMin;
    private Integer scrollSlotsMin;

    /**
     * Returns priceTypes, defaulting to all three when null or empty
     * so that an unchecked form still returns all currencies.
     */
    public List<String> getEffectivePriceTypes() {
        if (priceTypes == null || priceTypes.isEmpty()) {
            return List.of("MESO", "CS", "WS");
        }
        return priceTypes;
    }
}
