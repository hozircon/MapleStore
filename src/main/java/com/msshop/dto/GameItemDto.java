package com.msshop.dto;

import com.msshop.domain.GameItem;

/**
 * Lightweight read-only view of a GameItem, used in REST responses
 * and autocomplete suggestions.
 */
public record GameItemDto(Integer itemId, String name, String wzCategory) {

    public static GameItemDto from(GameItem g) {
        return new GameItemDto(g.getItemId(), g.getName(), g.getWzCategory());
    }
}
