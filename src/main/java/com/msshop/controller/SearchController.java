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
        List<ItemDto> items = searchService.search(searchRequest);

        model.addAttribute("items",         items);
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("totalCount",    items.size());
        model.addAttribute("hasResults",    !items.isEmpty());
        model.addAttribute("categories",    Category.values());

        return "index";
    }
}
