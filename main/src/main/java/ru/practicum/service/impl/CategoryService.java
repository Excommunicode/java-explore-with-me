package ru.practicum.service.impl;

import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    /**
     * Adds a new category to the system.
     *
     * @param categoryDto the data transfer object containing category details to be added.
     * @return the added {@link CategoryDto} with potentially additional data filled out by the system, such as an auto-generated ID.
     */
    CategoryDto addCategoryDto(CategoryDto categoryDto);

    /**
     * Updates an existing category identified by its ID with new details provided in the {@link CategoryDto}.
     *
     * @param id the ID of the category to update.
     * @param categoryDto the data transfer object containing updated details for the category.
     * @return the updated {@link CategoryDto}.
     */
    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    /**
     * Deletes a category from the system identified by its ID.
     *
     * @param id the ID of the category to be deleted.
     */
    void deleteCategory(Long id);

    /**
     * Retrieves a list of categories, supporting pagination by specifying a starting point and the number of records to fetch.
     *
     * @param from the starting index for fetching records.
     * @param size the number of categories to retrieve.
     * @return a list of {@link CategoryDto} based on pagination parameters.
     */
    List<CategoryDto> getCategories(int from, int size);

    /**
     * Retrieves details of a specific category identified by its ID.
     *
     * @param id the ID of the category to retrieve.
     * @return the {@link CategoryDto} containing detailed information about the category, if found.
     */
    CategoryDto getCategoryDto(Long id);
}