package com.example.personalblog.service;

import com.example.personalblog.exception.NotFoundException;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found";
    private static final String POST_NOT_FOUND_MSG = "Post not found";

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public CategoryService(CategoryRepository categoryRepository, PostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));
        category.setName(categoryDetails.getName());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));
        categoryRepository.delete(category);
    }

    @Transactional
    public Post addCategoryToPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));

        post.getCategories().add(category);
        return postRepository.save(post);
    }

    @Transactional
    public void removeCategoryFromPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));

        post.getCategories().remove(category);
        postRepository.save(post);
    }
}