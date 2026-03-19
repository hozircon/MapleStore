package com.msshop.controller;

import com.msshop.domain.Category;
import com.msshop.dto.ItemDto;
import com.msshop.dto.SearchRequest;
import com.msshop.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/")
    public String search(@ModelAttribute SearchRequest searchRequest, Model model) {
        boolean hasQuery = (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isBlank())
                || (searchRequest.getCategory() != null && !searchRequest.getCategory().isBlank())
                || (searchRequest.getEquipType() != null && !searchRequest.getEquipType().isBlank())
                || (searchRequest.getEquipSubType() != null && !searchRequest.getEquipSubType().isBlank())
                || (searchRequest.getSubCategory() != null && !searchRequest.getSubCategory().isBlank())
                || (searchRequest.getPriceTypes() != null && !searchRequest.getPriceTypes().isEmpty())
                || searchRequest.getStrMin() != null || searchRequest.getDexMin() != null
                || searchRequest.getIntMin() != null || searchRequest.getLukMin() != null
                || searchRequest.getAtkMin() != null || searchRequest.getMatkMin() != null
                || searchRequest.getScrollSlotsMin() != null;

        List<ItemDto> items = hasQuery ? searchService.search(searchRequest) : List.of();

        model.addAttribute("items",         items);
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("totalCount",    items.size());
        model.addAttribute("hasResults",    !items.isEmpty());
        model.addAttribute("searched",      hasQuery);
        model.addAttribute("categories",    Category.values());

        return "index";
    }
}
