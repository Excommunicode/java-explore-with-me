package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.impl.CategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        System.err.println("Request to create new category: " + categoryDto);
        CategoryDto createdCategory = categoryService.addCategoryDto(categoryDto);
        System.err.println("Category created successfully: " + createdCategory);
        return createdCategory;
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        System.err.println("Request to update category with ID " + catId + ": " + categoryDto);
        CategoryDto updatedCategory = categoryService.updateCategory(catId, categoryDto);
        System.err.println("Category updated successfully: " + updatedCategory);
        return updatedCategory;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        System.err.println("Request to delete category with ID: " + catId);
        categoryService.deleteCategory(catId);
        System.err.println("Category deleted successfully");
    }
}
