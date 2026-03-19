package com.msshop.controller;

import com.msshop.domain.Category;
import com.msshop.domain.ItemStatus;
import com.msshop.domain.PriceType;
import com.msshop.dto.AdminItemRequest;
import com.msshop.dto.ItemDto;
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

    // ===== Dashboard =====

    @GetMapping
    public String dashboard(
            @RequestParam(required = false) Boolean created,
            @RequestParam(required = false) Boolean updated,
            @RequestParam(required = false) Boolean soldout,
            @RequestParam(required = false) Boolean bulkDeleted,
            Model model) {

        List<ItemDto> items = itemService.findAll();
        model.addAttribute("items",        items);
        model.addAttribute("inStockCount", itemService.countByStatus(ItemStatus.IN_STOCK));
        model.addAttribute("soldOutCount", itemService.countByStatus(ItemStatus.SOLD_OUT));
        model.addAttribute("flashMessage", buildFlash(created, updated, soldout, bulkDeleted));
        return "admin/dashboard";
    }

    // ===== New item form =====

    @GetMapping("/items/new")
    public String newForm(Model model) {
        model.addAttribute("item",       new AdminItemRequest());
        model.addAttribute("categories", Category.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("isEdit",     false);
        return "admin/item-form";
    }

    // ===== Create item =====

    @PostMapping("/items")
    public String create(@Valid @ModelAttribute("item") AdminItemRequest req,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("priceTypes", PriceType.values());
            model.addAttribute("isEdit",     false);
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
        model.addAttribute("item",       req);
        model.addAttribute("itemId",     id);
        model.addAttribute("categories", Category.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("isEdit",     true);
        return "admin/item-form";
    }

    // ===== Update item =====

    @PostMapping("/items/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("item") AdminItemRequest req,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("itemId",     id);
            model.addAttribute("categories", Category.values());
            model.addAttribute("priceTypes", PriceType.values());
            model.addAttribute("isEdit",     true);
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
        req.setStrBonus(item.getStrBonus());
        req.setDexBonus(item.getDexBonus());
        req.setIntBonus(item.getIntBonus());
        req.setLukBonus(item.getLukBonus());
        req.setAtkBonus(item.getAtkBonus());
        req.setMatkBonus(item.getMatkBonus());
        req.setScrollSlotsRemaining(item.getScrollSlotsRemaining());
        req.setPriceType(item.getPriceType().name());
        req.setPriceValue(item.getPriceValue());
        req.setQuantity(item.getQuantity());
        req.setLocation(item.getLocation());
        req.setSellerName(item.getSellerName());
        return req;
    }
}
