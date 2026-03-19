package com.msshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Serves item icon images extracted from MapleStory WZ resources.
 *
 * <p>Endpoint: {@code GET /images/items/{itemId}.png}
 *
 * <p>Search order:
 * <ol>
 *   <li>In-memory path cache (after first lookup)</li>
 *   <li>Item.wz — Consume / Etc / Cash / Install / Special / Pet</li>
 *   <li>Character_0.wz — all equipment subcategories</li>
 * </ol>
 *
 * <p>Returns 404 (no body) when the icon is not found; the browser will fall
 * back to the {@code onerror} placeholder already set in the HTML template.
 */
@RestController
public class ItemIconController {

    private static final List<String> ITEM_CATEGORIES =
            List.of("Consume", "Etc", "Cash", "Install", "Special", "Pet");

    private static final List<String> CHAR_SUBCATEGORIES =
            List.of("Cap", "Cape", "Coat", "Longcoat", "Pants", "Shoes",
                    "Glove", "Shield", "Weapon", "Accessory", "Ring",
                    "Face", "Hair", "PetEquip", "TamingMob", "Afterimage");

    /** Caches resolved paths so repeated requests skip the disk scan. */
    private final ConcurrentHashMap<Integer, Path> pathCache = new ConcurrentHashMap<>();
    /** Sentinel value — means "not found, don't search again". */
    private static final Path NOT_FOUND_SENTINEL = Path.of("");

    private final Path itemBase;
    private final Path charBase;

    public ItemIconController(
            @Value("${app.wz.item-path}") String itemPath,
            @Value("${app.wz.char-path}") String charPath) {
        this.itemBase = Path.of(itemPath);
        this.charBase = Path.of(charPath);
    }

    @GetMapping("/images/items/{itemId}.png")
    public ResponseEntity<byte[]> getIcon(@PathVariable Integer itemId) throws IOException {
        Path cached = pathCache.get(itemId);

        if (cached == NOT_FOUND_SENTINEL) {
            return ResponseEntity.notFound().build();
        }

        if (cached == null) {
            cached = resolve(itemId);
            pathCache.put(itemId, cached == null ? NOT_FOUND_SENTINEL : cached);
        }

        if (cached == null || cached == NOT_FOUND_SENTINEL) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(cached);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(bytes);
    }

    /** Finds the icon file on disk; returns {@code null} if not found. */
    private Path resolve(int itemId) {
        String padded = String.format("%08d", itemId);
        String prefix4 = padded.substring(0, 4);

        // ── Item.wz ───────────────────────────────────────────────────────────
        // Structure: {Category}/{Category}/{prefix4}.img/{prefix4}.img/{padded}.info.icon.png
        for (String category : ITEM_CATEGORIES) {
            Path p = itemBase
                    .resolve(category).resolve(category)
                    .resolve(prefix4 + ".img").resolve(prefix4 + ".img")
                    .resolve(padded + ".info.icon.png");
            if (Files.exists(p)) return p;
        }

        // ── Character_0.wz ────────────────────────────────────────────────────
        // Structure: {SubCategory}/{SubCategory}/{padded}.img/{padded}.img/info.icon.png
        for (String subcat : CHAR_SUBCATEGORIES) {
            Path p = charBase
                    .resolve(subcat).resolve(subcat)
                    .resolve(padded + ".img").resolve(padded + ".img")
                    .resolve("info.icon.png");
            if (Files.exists(p)) return p;
        }

        return null;
    }
}
