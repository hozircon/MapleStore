package com.msshop.controller;

import com.msshop.domain.Category;
import com.msshop.domain.ItemStatus;
import com.msshop.domain.PriceType;
import com.msshop.dto.AdminItemRequest;
import com.msshop.dto.ItemDto;
import com.msshop.service.AnnouncementService;
import com.msshop.service.CharacterPresetService;
import com.msshop.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ItemService itemService;
    private final AnnouncementService announcementService;
    private final CharacterPresetService characterPresetService;

    // ===== Dashboard =====

    @GetMapping
    public String dashboard(
            @RequestParam(required = false) Boolean created,
            @RequestParam(required = false) Boolean updated,
            @RequestParam(required = false) Boolean soldout,
            @RequestParam(required = false) Boolean bulkDeleted,
            @RequestParam(required = false) String filterName,
            @RequestParam(required = false) String filterCategory,
            @RequestParam(required = false) String filterSubCategory,
            @RequestParam(required = false) String filterPriceType,
            @RequestParam(required = false) String filterSeller,
            @RequestParam(required = false) String filterWarehouse,
            @RequestParam(required = false) String filterLocation,
            @RequestParam(required = false) String filterStatus,
            Model model) {

        List<ItemDto> items = itemService.findAllFiltered(filterName, filterCategory, filterSubCategory,
                filterPriceType, filterSeller, filterWarehouse, filterLocation, filterStatus);
        model.addAttribute("items",             items);
        model.addAttribute("inStockCount",      itemService.countByStatus(ItemStatus.IN_STOCK));
        model.addAttribute("soldOutCount",      itemService.countByStatus(ItemStatus.SOLD_OUT));
        model.addAttribute("flashMessage",      buildFlash(created, updated, soldout, bulkDeleted));
        model.addAttribute("categories",        Category.values());
        model.addAttribute("priceTypes",        PriceType.values());
        model.addAttribute("statuses",          ItemStatus.values());
        model.addAttribute("announcements",     announcementService.findAll());
        model.addAttribute("characterPresets",  characterPresetService.findAll());
        // Keep filter values in form
        model.addAttribute("filterName",        filterName);
        model.addAttribute("filterCategory",    filterCategory);
        model.addAttribute("filterSubCategory", filterSubCategory);
        model.addAttribute("filterPriceType",   filterPriceType);
        model.addAttribute("filterSeller",      filterSeller);
        model.addAttribute("filterWarehouse",   filterWarehouse);
        model.addAttribute("filterLocation",    filterLocation);
        model.addAttribute("filterStatus",      filterStatus);
        return "admin/dashboard";
    }

    // ===== New item form =====

    @GetMapping("/items/new")
    public String newForm(Model model) {
        model.addAttribute("item",            new AdminItemRequest());
        model.addAttribute("categories",      Category.values());
        model.addAttribute("priceTypes",      PriceType.values());
        model.addAttribute("isEdit",          false);
        model.addAttribute("characterNames",  characterPresetService.findAllNames());
        return "admin/item-form";
    }

    // ===== Create item =====

    @PostMapping("/items")
    public String create(@Valid @ModelAttribute("item") AdminItemRequest req,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categories",     Category.values());
            model.addAttribute("priceTypes",     PriceType.values());
            model.addAttribute("isEdit",         false);
            model.addAttribute("characterNames",  characterPresetService.findAllNames());
            return "admin/item-form";
        }
        itemService.create(req);
        return "redirect:/admin?created=true";
    }

    // ===== Edit item form =====

    @GetMapping("/items/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        com.msshop.domain.Item item = itemService.findById(id);
        AdminItemRequest req = toRequest(item);
        model.addAttribute("item",            req);
        model.addAttribute("itemId",          id);
        model.addAttribute("categories",      Category.values());
        model.addAttribute("priceTypes",      PriceType.values());
        model.addAttribute("isEdit",          true);
        model.addAttribute("characterNames",  characterPresetService.findAllNames());
        return "admin/item-form";
    }

    // ===== Update item =====

    @PostMapping("/items/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("item") AdminItemRequest req,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("itemId",         id);
            model.addAttribute("categories",     Category.values());
            model.addAttribute("priceTypes",     PriceType.values());
            model.addAttribute("isEdit",         true);
            model.addAttribute("characterNames",  characterPresetService.findAllNames());
            return "admin/item-form";
        }
        itemService.update(id, req);
        return "redirect:/admin?updated=true";
    }

    // ===== Mark sold-out =====

    @PostMapping("/items/{id}/soldout")
    public String markSoldOut(@PathVariable Long id) {
        itemService.markSoldOut(id);
        return "redirect:/admin?soldout=true";
    }

    // ===== Bulk delete sold-out =====

    @PostMapping("/items/soldout/delete")
    public String bulkDeleteSoldOut() {
        itemService.deleteAllSoldOut();
        return "redirect:/admin?bulkDeleted=true";
    }

    // ===== Announcements =====

    @PostMapping("/announcements")
    public String createAnnouncement(@RequestParam String message) {
        if (message != null && !message.isBlank()) {
            announcementService.create(message.trim());
        }
        return "redirect:/admin";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id) {
        announcementService.delete(id);
        return "redirect:/admin";
    }

    // ===== Character presets =====

    @PostMapping("/characters")
    public String createCharacter(@RequestParam String name) {
        characterPresetService.create(name);
        return "redirect:/admin";
    }

    @PostMapping("/characters/{id}/delete")
    public String deleteCharacter(@PathVariable Long id) {
        characterPresetService.delete(id);
        return "redirect:/admin";
    }

    // ---- helpers ----

    private String buildFlash(Boolean created, Boolean updated,
                               Boolean soldout, Boolean bulkDeleted) {
        if (Boolean.TRUE.equals(created))     return "✅ 商品已新增";
        if (Boolean.TRUE.equals(updated))     return "✅ 商品已更新";
        if (Boolean.TRUE.equals(soldout))     return "✅ 已標記為售完";
        if (Boolean.TRUE.equals(bulkDeleted)) return "✅ 已清除所有售完商品";
        return null;
    }

    private AdminItemRequest toRequest(com.msshop.domain.Item item) {
        AdminItemRequest req = new AdminItemRequest();
        req.setItemId(item.getItemId());
        req.setName(item.getName());
        req.setCategory(item.getCategory().name());
        req.setSubCategory(item.getSubCategory());
        req.setEquipType(item.getEquipType());
        req.setEquipSubType(item.getEquipSubType());
        req.setStrBonus(item.getStrBonus());
        req.setDexBonus(item.getDexBonus());
        req.setIntBonus(item.getIntBonus());
        req.setLukBonus(item.getLukBonus());
        req.setAtkBonus(item.getAtkBonus());
        req.setMatkBonus(item.getMatkBonus());
        req.setScrollSlotsRemaining(item.getScrollSlotsRemaining());
        req.setOtherStats(item.getOtherStats());
        req.setPriceType(item.getPriceType().name());
        req.setPriceValue(item.getPriceValue());
        req.setQuantity(item.getQuantity());
        req.setLocation(item.getLocation());
        req.setSellerName(item.getSellerName());
        req.setWarehouseChar(item.getWarehouseChar());
        return req;
    }
}
