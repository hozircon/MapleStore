package com.msshop.service;

import com.msshop.domain.Category;
import com.msshop.domain.Item;
import com.msshop.domain.ItemStatus;
import com.msshop.dto.ItemDto;
import com.msshop.dto.SearchRequest;
import com.msshop.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public List<ItemDto> search(SearchRequest req) {
        StringBuilder jpql = new StringBuilder(
            "SELECT i FROM Item i WHERE i.status = :status");

        Map<String, Object> params = new HashMap<>();
        params.put("status", ItemStatus.IN_STOCK);

        // Keyword (fuzzy match on name, case-insensitive)
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            jpql.append(" AND LOWER(i.name) LIKE :keyword");
            params.put("keyword", "%" + req.getKeyword().toLowerCase() + "%");
        }

        // Category filter
        Category cat = null;
        if (req.getCategory() != null && !req.getCategory().isBlank()) {
            try {
                cat = Category.valueOf(req.getCategory());
                jpql.append(" AND i.category = :category");
                params.put("category", cat);
            } catch (IllegalArgumentException ignored) {
                // unknown value — ignore filter
            }
        }

        // Sub-category filter (only for non-equipment categories)
        if (cat != Category.EQUIPMENT
                && req.getSubCategory() != null
                && !req.getSubCategory().isBlank()) {
            jpql.append(" AND i.subCategory = :subCategory");
            params.put("subCategory", req.getSubCategory());
        }

        // Currency filter
        List<String> priceTypes = req.getEffectivePriceTypes();
        jpql.append(" AND i.priceType IN :priceTypes");
        params.put("priceTypes", priceTypes.stream()
                .map(pt -> {
                    try { return com.msshop.domain.PriceType.valueOf(pt); }
                    catch (IllegalArgumentException e) { return null; }
                })
                .filter(pt -> pt != null)
                .toList());

        // Equipment stat threshold filters (AND logic, only when category = EQUIPMENT)
        if (cat == Category.EQUIPMENT) {
            if (req.getStrMin() != null) {
                jpql.append(" AND i.strBonus >= :strMin");
                params.put("strMin", req.getStrMin());
            }
            if (req.getDexMin() != null) {
                jpql.append(" AND i.dexBonus >= :dexMin");
                params.put("dexMin", req.getDexMin());
            }
            if (req.getIntMin() != null) {
                jpql.append(" AND i.intBonus >= :intMin");
                params.put("intMin", req.getIntMin());
            }
            if (req.getLukMin() != null) {
                jpql.append(" AND i.lukBonus >= :lukMin");
                params.put("lukMin", req.getLukMin());
            }
            if (req.getAtkMin() != null) {
                jpql.append(" AND i.atkBonus >= :atkMin");
                params.put("atkMin", req.getAtkMin());
            }
            if (req.getMatkMin() != null) {
                jpql.append(" AND i.matkBonus >= :matkMin");
                params.put("matkMin", req.getMatkMin());
            }
            if (req.getScrollSlotsMin() != null) {
                jpql.append(" AND i.scrollSlotsRemaining >= :scrollSlotsMin");
                params.put("scrollSlotsMin", req.getScrollSlotsMin());
            }
        }

        // Currency-priority sort: MESO(1) → CS(2) → WS(3), then price ASC
        jpql.append("""
             ORDER BY
               CASE i.priceType
                 WHEN com.msshop.domain.PriceType.MESO THEN 1
                 WHEN com.msshop.domain.PriceType.CS   THEN 2
                 ELSE 3
               END ASC,
               i.priceValue ASC
            """);

        TypedQuery<Item> query = em.createQuery(jpql.toString(), Item.class);
        params.forEach(query::setParameter);

        return query.getResultList().stream()
                .map(ItemDto::from)
                .toList();
    }
}
