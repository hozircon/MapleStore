package com.msshop.repository;

import com.msshop.domain.GameItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameItemRepository extends JpaRepository<GameItem, Integer> {

    /**
     * Full-name keyword search, returns up to whatever the caller limits.
     * Ordered by itemId for stable results.
     */
    @Query("SELECT g FROM GameItem g WHERE g.name LIKE %:keyword% ORDER BY g.itemId")
    List<GameItem> searchByName(@Param("keyword") String keyword);

    /**
     * Exact-prefix lookup (e.g. "黑色" matches "黑色勇士戒指")
     * used as a tighter alternative when needed.
     */
    @Query("SELECT g FROM GameItem g WHERE g.name LIKE :prefix% ORDER BY g.itemId")
    List<GameItem> searchByNamePrefix(@Param("prefix") String prefix);
}
