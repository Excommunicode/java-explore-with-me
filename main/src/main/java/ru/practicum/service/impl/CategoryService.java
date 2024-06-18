package ru.practicum.service.impl;

import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategoryDto(CategoryDto categoryDto);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    void deleteCategory(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryDto(Long id);
}
