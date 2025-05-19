package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateCategoryRequest;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CreateCategoryRequest createRequest;
    private Post post;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        createRequest = new CreateCategoryRequest();
        createRequest.setName("Test Category");

        post = new Post();
        post.setId(1L);
        Set<Category> categories = new HashSet<>();
        categories.add(category);
        post.setCategories(categories);
        category.setPosts(Set.of(post));
    }

    @Test
    void createCategory_ShouldCreateNewCategory() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowConflictWhenNameExists() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void getCategoryById_ShouldThrowNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        List<Category> categories = List.of(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1).containsExactly(category);
    }

    @Test
    void updateCategory_ShouldUpdateExistingCategory() {
        Category updated = new Category();
        updated.setName("Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Updated Name")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        Category result = categoryService.updateCategory(1L, updated);

        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void updateCategory_ShouldNotUpdateWhenNameNotChanged() {
        Category sameName = new Category();
        sameName.setName("Test Category");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.updateCategory(1L, sameName);

        assertThat(result.getName()).isEqualTo("Test Category");
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void updateCategory_ShouldThrowConflictWhenNameExists() {
        Category updated = new Category();
        updated.setName("Existing Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Existing Name")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, updated))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void updateCategory_ShouldThrowNotFound() {
        Category updated = new Category();
        updated.setName("Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(1L, updated))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteCategory_ShouldDeleteCategoryAndUpdatePosts() {
        // Подготовка тестовых данных
        Post post = new Post();
        post.setId(1L);
        Set<Category> postCategories = new HashSet<>();
        postCategories.add(category);
        post.setCategories(postCategories);

        category.setPosts(Set.of(post));

        // Настройка моков
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            assertThat(savedPost.getCategories()).isEmpty(); // Проверяем, что категория удалена из поста
            return savedPost;
        });

        // Вызов тестируемого метода
        categoryService.deleteCategory(1L);

        // Проверки
        verify(postRepository).save(post);
        verify(categoryRepository).delete(category);
        verify(cacheService).invalidateByPrefix("posts:");

        // Дополнительная проверка, что категория больше не связана с постом
        assertThat(post.getCategories()).isEmpty();
    }

    @Test
    void deleteCategory_ShouldHandleEmptyPosts() {
        category.setPosts(Collections.emptySet());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(postRepository, never()).save(any());
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_ShouldThrowNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }
}