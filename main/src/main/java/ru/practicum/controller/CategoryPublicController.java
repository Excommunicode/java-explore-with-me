package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.impl.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategoriesDto(@RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        System.err.println("Starting to fetch categories from index " + from + " with size " + size);
        List<CategoryDto> categories = categoryService.getCategories(from, size);
        System.err.println("Completed fetching categories. Total categories fetched: " + categories.size());
        return categories;
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryDto(@PathVariable Long catId) {
        System.err.println("Starting to fetch category with ID " + catId);
        CategoryDto categoryDto = categoryService.getCategoryDto(catId);
        System.err.println("Category fetched successfully: " + categoryDto.getName());
        return categoryDto;
    }
}