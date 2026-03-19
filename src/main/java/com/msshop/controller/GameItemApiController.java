package com.msshop.controller;

import com.msshop.dto.GameItemDto;
import com.msshop.service.GameItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for GameItem catalog lookup.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/game-items?q=戒指}  – keyword search (max 20 results)</li>
 *   <li>{@code GET /api/game-items/{itemId}} – single item by 7-digit ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/game-items")
@RequiredArgsConstructor
public class GameItemApiController {

    private final GameItemService gameItemService;

    /**
     * Keyword search for item names.
     *
     * @param q search keyword (empty string returns empty list)
     */
    @GetMapping
    public List<GameItemDto> search(@RequestParam(name = "q", defaultValue = "") String q) {
        return gameItemService.search(q);
    }

    /**
     * Fetch a single item by its 7-digit MapleStory item ID.
     *
     * @return 200 with the item, or 404 if not in catalog
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<GameItemDto> getById(@PathVariable Integer itemId) {
        return gameItemService.findById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
