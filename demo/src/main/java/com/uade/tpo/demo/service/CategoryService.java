
package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
import com.uade.tpo.demo.exceptions.CategoryInUseException;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;

public interface CategoryService {
    public Page<Category> getCategories(PageRequest pageRequest);

    public Optional<Category> getCategoryById(Long categoryId);

    public Category createCategory(String description) throws CategoryDuplicateException;

    public Category updateCategory(Long categoryId, String description)
            throws CategoryDuplicateException, CategoryNotFoundException;

    public void deleteCategory(Long categoryId) throws CategoryNotFoundException, CategoryInUseException;
}