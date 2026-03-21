package com.msshop.service;

import com.msshop.domain.Category;
import com.msshop.domain.Item;
import com.msshop.domain.ItemStatus;
import com.msshop.domain.PriceType;
import com.msshop.dto.AdminItemRequest;
import com.msshop.dto.ItemDto;
import com.msshop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    /** All items (admin view), newest first. */
    @Transactional(readOnly = true)
    public List<ItemDto> findAll() {
        return itemRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ItemDto::from).toList();
    }

    /** Filtered items for admin dashboard, newest first. */
    @Transactional(readOnly = true)
    public List<ItemDto> findAllFiltered(String name, String category, String subCategory,
                                         String priceType, String seller,
                                         String warehouse, String location, String status) {
        Specification<Item> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (name != null && !name.isBlank())
                p.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            if (category != null && !category.isBlank()) {
                try { p.add(cb.equal(root.get("category"), Category.valueOf(category))); }
                catch (IllegalArgumentException ignored) {}
            }
            if (subCategory != null && !subCategory.isBlank())
                p.add(cb.like(cb.lower(root.get("subCategory")), "%" + subCategory.toLowerCase() + "%"));
            if (priceType != null && !priceType.isBlank()) {
                try { p.add(cb.equal(root.get("priceType"), PriceType.valueOf(priceType))); }
                catch (IllegalArgumentException ignored) {}
            }
            if (seller != null && !seller.isBlank())
                p.add(cb.like(cb.lower(root.get("sellerName")), "%" + seller.toLowerCase() + "%"));
            if (warehouse != null && !warehouse.isBlank())
                p.add(cb.like(cb.lower(root.get("warehouseChar")), "%" + warehouse.toLowerCase() + "%"));
            if (location != null && !location.isBlank())
                p.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            if (status != null && !status.isBlank()) {
                try { p.add(cb.equal(root.get("status"), ItemStatus.valueOf(status))); }
                catch (IllegalArgumentException ignored) {}
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return itemRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("商品不存在 id=" + id));
    }

    /** Create a new item with IN_STOCK status. */
    @Transactional
    public Item create(AdminItemRequest req) {
        Item item = buildItem(new Item(), req);
        item.setStatus(ItemStatus.IN_STOCK);
        return itemRepository.save(item);
    }

    /** Update an existing item's editable fields. */
    @Transactional
    public Item update(Long id, AdminItemRequest req) {
        Item item = findById(id);
        buildItem(item, req);
        return itemRepository.save(item);
    }

    /** Mark a single item as SOLD_OUT. */
    @Transactional
    public void markSoldOut(Long id) {
        Item item = findById(id);
        item.setStatus(ItemStatus.SOLD_OUT);
        itemRepository.save(item);
    }

    /** Bulk-delete all SOLD_OUT items. Returns the count deleted. */
    @Transactional
    public int deleteAllSoldOut() {
        return itemRepository.deleteAllByStatus(ItemStatus.SOLD_OUT);
    }

    public int countByStatus(ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    public List<String> findDistinctWarehouseChars() {
        return itemRepository.findDistinctWarehouseChars();
    }

    public List<String> findDistinctSellerNames() {
        return itemRepository.findDistinctSellerNames();
    }

    // ---- helpers ----

    private Item buildItem(Item item, AdminItemRequest req) {
        item.setItemId(req.getItemId());
        item.setName(req.getName());
        item.setCategory(Category.valueOf(req.getCategory()));
        item.setSubCategory(req.getSubCategory());
        item.setEquipType(req.getEquipType());
        item.setEquipSubType(req.getEquipSubType());
        item.setStrBonus(req.getStrBonus());
        item.setDexBonus(req.getDexBonus());
        item.setIntBonus(req.getIntBonus());
        item.setLukBonus(req.getLukBonus());
        item.setAtkBonus(req.getAtkBonus());
        item.setMatkBonus(req.getMatkBonus());
        item.setScrollSlotsRemaining(req.getScrollSlotsRemaining());
        item.setOtherStats(req.getOtherStats());
        item.setPriceType(PriceType.valueOf(req.getPriceType()));
        item.setPriceValue(req.getPriceValue());
        item.setQuantity(req.getQuantity());
        item.setLocation(req.getLocation() == null || req.getLocation().isBlank()
                ? "請跟賣家聯絡" : req.getLocation());
        item.setSellerName(req.getSellerName());
        item.setWarehouseChar(req.getWarehouseChar());
        return item;
    }
}
