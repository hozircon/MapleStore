package com.msshop.service;

import com.msshop.domain.GameItem;
import com.msshop.dto.GameItemDto;
import com.msshop.repository.GameItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameItemService {

    private static final int SEARCH_LIMIT = 20;

    private final GameItemRepository gameItemRepository;

    /**
     * Keyword search across item names. Returns at most {@value SEARCH_LIMIT}
     * results ordered by itemId.
     */
    @Transactional(readOnly = true)
    public List<GameItemDto> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return gameItemRepository.searchByName(keyword.trim())
                .stream()
                .limit(SEARCH_LIMIT)
                .map(GameItemDto::from)
                .toList();
    }

    /** Look up a single item by its 7-digit ID. */
    @Transactional(readOnly = true)
    public Optional<GameItemDto> findById(Integer itemId) {
        return gameItemRepository.findById(itemId).map(GameItemDto::from);
    }

    /** Total number of catalog entries loaded (useful for health checks). */
    @Transactional(readOnly = true)
    public long count() {
        return gameItemRepository.count();
    }
}
