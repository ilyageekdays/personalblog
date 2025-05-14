package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateCategoryRequest;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found";

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CacheService cacheService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, PostRepository postRepository, CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = new Category();

        if (categoryRepository.existsByName(createCategoryRequest.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Name already exists"
            );
        }

        category.setName(createCategoryRequest.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND_MSG));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category updateCategory(Long id, Category updateCategoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        CATEGORY_NOT_FOUND_MSG));

        if (!category.getName().equals(updateCategoryRequest.getName())) {
            if (categoryRepository.existsByName(updateCategoryRequest.getName())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Title already exists"
                );
            }
        }

        category.setName(updateCategoryRequest.getName());
        cacheService.invalidateByPrefix("posts:");
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        CATEGORY_NOT_FOUND_MSG));

        Set<Post> posts = new HashSet<>(category.getPosts());
        for (Post post : posts) {
            post.getCategories().remove(category);
            postRepository.save(post);
        }
        cacheService.invalidateByPrefix("posts:");
        categoryRepository.delete(category);
    }
}