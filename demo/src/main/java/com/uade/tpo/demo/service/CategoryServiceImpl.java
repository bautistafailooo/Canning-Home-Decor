package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
import com.uade.tpo.demo.exceptions.CategoryInUseException;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Page<Category> getCategories(PageRequest pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category createCategory(String description) throws CategoryDuplicateException {
        List<Category> categories = categoryRepository.findByDescription(description);
        if (categories.isEmpty())
            return categoryRepository.save(new Category(description));
        throw new CategoryDuplicateException();
    }

    // Antes hacia .orElse(null) y despues usaba la variable: con un id
    // inexistente eso era un NullPointerException seguro, que llegaba al
    // front como un 500 sin explicacion.
    public Category updateCategory(Long categoryId, String description)
            throws CategoryDuplicateException, CategoryNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        // Renombrar a un nombre que ya existe se rechaza igual que al crear.
        // Se excluye la propia categoria para que guardar sin cambiar el
        // nombre no se tome como duplicado.
        boolean nombreOcupado = categoryRepository.findByDescription(description).stream()
                .anyMatch(otra -> !otra.getId().equals(categoryId));
        if (nombreOcupado)
            throw new CategoryDuplicateException();

        category.setDescription(description);
        return categoryRepository.save(category);
    }

    // Antes borraba derecho. Si la categoria tenia productos, la clave
    // foranea de product.category_id hacia fallar el DELETE en la base y
    // salia un 500. Ahora se avisa con un motivo entendible.
    public void deleteCategory(Long categoryId) throws CategoryNotFoundException, CategoryInUseException {
        if (!categoryRepository.existsById(categoryId))
            throw new CategoryNotFoundException();

        if (productRepository.countByCategoryId(categoryId) > 0)
            throw new CategoryInUseException();

        categoryRepository.deleteById(categoryId);
    }
}
