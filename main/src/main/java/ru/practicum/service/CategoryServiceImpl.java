package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.impl.CategoryService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategoryDto(CategoryDto categoryDto) {
        log.debug("Adding new category: {}", categoryDto);
        checkName(categoryDto.getName());
        CategoryDto result = categoryMapper.toDto(categoryRepository.save(categoryMapper.toModel(categoryDto)));
        log.info("Category added with ID: {}", result.getId());
        return result;
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        log.debug("Updating category with ID: {}", id);
        CategoryDto dto = getDto(id);
        checkName(categoryDto, dto);
        CategoryDto updatedDto = categoryMapper.toDto(categoryRepository.save(categoryMapper.toModel(dto)));
        log.info("Category updated with new values: {}", updatedDto);
        return updatedDto;
    }

    @Transactional
    @Override
    public void deleteCategory(Long id) {
        log.debug("Deleting category with ID: {}", id);
        existCategoryInEvent(id);
        categoryRepository.deleteById(id);
        log.info("Category deleted with ID: {}", id);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.debug("Fetching categories from: {}, size: {}", from, size);
        Pageable pageable = PageRequest.of(from, size);
        List<CategoryDto> categories = categoryMapper.toDtoList(categoryRepository.findAll(pageable).getContent());
        log.info("Fetched categories: {}", categories);
        return categories;
    }

    @Override
    public CategoryDto getCategoryDto(Long id) {
        log.debug("Fetching category with ID: {}", id);
        CategoryDto categoryDto = getDto(id);
        log.info("Fetched category: {}", categoryDto);
        return categoryDto;
    }

    private void checkName(CategoryDto categoryDto, CategoryDto dto) {
        if (!categoryDto.getName().equals(dto.getName())) {
            checkName(categoryDto.getName());
            dto.setName(categoryDto.getName());
        } else {
            if (categoryDto.getName() != null) {
                dto.setName(categoryDto.getName());
            }
        }
    }

    private void existCategoryInEvent(Long id) {
        if (eventRepository.existsByCategory_Id(id)) {
            throw new ConflictException(String.format("Category with id %s is already associated with an event", id));
        }
    }

    private CategoryDto getDto(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", id);
                    return new NotFoundException("Not found category");
                }));
    }

    private void checkName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException(String.format("This name :%s has already taken", name));
        }
    }
}