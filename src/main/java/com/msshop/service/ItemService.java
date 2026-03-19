package com.msshop.service;

import com.msshop.domain.Category;
import com.msshop.domain.Item;
import com.msshop.domain.ItemStatus;
import com.msshop.domain.PriceType;
import com.msshop.dto.AdminItemRequest;
import com.msshop.dto.ItemDto;
import com.msshop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ---- helpers ----

    private Item buildItem(Item item, AdminItemRequest req) {
        item.setItemId(req.getItemId());
        item.setName(req.getName());
        item.setCategory(Category.valueOf(req.getCategory()));
        item.setSubCategory(req.getSubCategory());
        item.setStrBonus(req.getStrBonus());
        item.setDexBonus(req.getDexBonus());
        item.setIntBonus(req.getIntBonus());
        item.setLukBonus(req.getLukBonus());
        item.setAtkBonus(req.getAtkBonus());
        item.setMatkBonus(req.getMatkBonus());
        item.setScrollSlotsRemaining(req.getScrollSlotsRemaining());
        item.setPriceType(PriceType.valueOf(req.getPriceType()));
        item.setPriceValue(req.getPriceValue());
        item.setQuantity(req.getQuantity());
        item.setLocation(req.getLocation());
        item.setSellerName(req.getSellerName());
        return item;
    }
}
