package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreatePostRequest;
import com.example.personalblog.dto.UpdatePostRequest;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.model.User;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import com.example.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private CreatePostRequest createPostRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);
        testPost.setCategories(new HashSet<>(Set.of(testCategory)));

        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post");
        createPostRequest.setContent("Test Content");
        createPostRequest.setCategoryNames(List.of("Technology"));
    }

    @Test
    void createPost_ShouldReturnCreatedPost() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.createPost(1L, createPostRequest);

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void createPost_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.createPost(1L, createPostRequest));
    }

    @Test
    void createPost_ShouldThrowWhenRequestIsNull() {
        assertThrows(NullPointerException.class, () ->
                postService.createPost(1L, null));
    }

    @Test
    void createPost_ShouldCreateNewCategoryWhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("NewCategory")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        createPostRequest.setCategoryNames(List.of("NewCategory"));
        Post result = postService.createPost(1L, createPostRequest);

        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createPost_ShouldHandleEmptyCategoryList() {
        createPostRequest.setCategoryNames(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Post savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("Test Post");
        savedPost.setContent("Test Content");
        savedPost.setAuthor(testUser);
        savedPost.setCategories(new HashSet<>());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        Post result = postService.createPost(1L, createPostRequest);

        assertNotNull(result);
        assertTrue(result.getCategories().isEmpty());
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void createPost_ShouldTrimCategoryNames() {
        createPostRequest.setCategoryNames(List.of("  Technology  "));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.createPost(1L, createPostRequest);

        assertNotNull(result);
        assertEquals("Technology", result.getCategories().iterator().next().getName());
        verify(categoryRepository).findByName("Technology");
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void createPostsBulk_ShouldHandleEmptyRequestList() {
        List<Post> result = postService.createPostsBulk(1L, Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(postRepository, never()).saveAll(anyList());
        verify(cacheService, never()).invalidateByPrefix(anyString());
    }

    @Test
    void getPosts_ShouldFetchFromDbWhenCacheEmpty() {
        when(cacheService.get("posts:author:testuser")).thenReturn(null);
        when(postRepository.findAllByAuthorUsername("testuser")).thenReturn(List.of(testPost));

        List<Post> result = postService.getPosts(null, "testuser");

        assertEquals(1, result.size());
        verify(cacheService).put("posts:author:testuser", List.of(testPost));
    }

    @Test
    void addCategoryToPost_ShouldAddCategory() {
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Science");

        Post postWithMutableCategories = new Post();
        postWithMutableCategories.setId(1L);
        postWithMutableCategories.setTitle("Test Post");
        postWithMutableCategories.setContent("Test Content");
        postWithMutableCategories.setAuthor(testUser);
        postWithMutableCategories.setCategories(new HashSet<>(Set.of(testCategory)));

        when(postRepository.findById(1L)).thenReturn(Optional.of(postWithMutableCategories));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.addCategoryToPost(1L, 2L);

        assertTrue(result.getCategories().contains(newCategory));
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void createPostsBulk_ShouldTrimCategoryNames() {
        createPostRequest.setCategoryNames(List.of("  Technology  "));
        List<CreatePostRequest> requests = List.of(createPostRequest);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(postRepository.saveAll(anyList())).thenReturn(List.of(testPost));

        List<Post> result = postService.createPostsBulk(1L, requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Technology", result.get(0).getCategories().iterator().next().getName());
        verify(categoryRepository).findByName("Technology");
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void createPostsBulk_ShouldReturnListOfCreatedPosts() {
        List<CreatePostRequest> requests = List.of(createPostRequest, createPostRequest);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(postRepository.saveAll(anyList())).thenReturn(List.of(testPost, testPost));

        List<Post> result = postService.createPostsBulk(1L, requests);

        assertEquals(2, result.size());
        verify(cacheService, times(1)).invalidateByPrefix("posts:");
    }

    @Test
    void createPostsBulk_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.createPostsBulk(1L, List.of(createPostRequest)));
    }

    @Test
    void createPostsBulk_ShouldHandleEmptyCategoryList() {
        createPostRequest.setCategoryNames(null);
        List<CreatePostRequest> requests = List.of(createPostRequest);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Post savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("Test Post");
        savedPost.setContent("Test Content");
        savedPost.setAuthor(testUser);
        savedPost.setCategories(new HashSet<>());
        when(postRepository.saveAll(anyList())).thenReturn(List.of(savedPost));

        List<Post> result = postService.createPostsBulk(1L, requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getCategories().isEmpty());
    }

    @Test
    void createPostsBulk_ShouldCreateMultiplePostsWithDifferentCategories() {
        CreatePostRequest secondRequest = new CreatePostRequest();
        secondRequest.setTitle("Second Post");
        secondRequest.setContent("Second Content");
        secondRequest.setCategoryNames(List.of("Science"));

        Category scienceCategory = new Category();
        scienceCategory.setId(2L);
        scienceCategory.setName("Science");

        Post secondPost = new Post();
        secondPost.setId(2L);
        secondPost.setTitle("Second Post");
        secondPost.setContent("Second Content");
        secondPost.setAuthor(testUser);
        secondPost.setCategories(new HashSet<>(Set.of(scienceCategory)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName("Science")).thenReturn(Optional.of(scienceCategory));
        when(postRepository.saveAll(anyList())).thenReturn(List.of(testPost, secondPost));

        List<Post> result = postService.createPostsBulk(1L, List.of(createPostRequest, secondRequest));

        assertEquals(2, result.size());
        assertEquals("Technology", result.get(0).getCategories().iterator().next().getName());
        assertEquals("Science", result.get(1).getCategories().iterator().next().getName());
    }

    @Test
    void updatePost_ShouldUpdateExistingPost() {
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");
        updateRequest.setCategoryNames(List.of("NewCategory"));

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(categoryRepository.findByName("NewCategory")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.updatePost(1L, updateRequest);

        assertNotNull(result);
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void updatePost_ShouldThrowWhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.updatePost(1L, new UpdatePostRequest()));
    }

    @Test
    void updatePost_ShouldHandleNullCategoryList() {
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");
        updateRequest.setCategoryNames(null);

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Updated Title");
        updatedPost.setContent("Updated Content");
        updatedPost.setAuthor(testUser);
        updatedPost.setCategories(new HashSet<>());

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        Post result = postService.updatePost(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertTrue(result.getCategories().isEmpty());
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void updatePost_ShouldHandleUnchangedFields() {
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Test Post");
        updateRequest.setContent("Test Content");
        updateRequest.setCategoryNames(List.of("Technology"));

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.updatePost(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(1, result.getCategories().size());
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void deletePost_ShouldDeleteExistingPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        postService.deletePost(1L);

        verify(postRepository).delete(testPost);
        verify(cacheService).invalidateByPrefix("posts:");
    }

    @Test
    void deletePost_ShouldThrowWhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.deletePost(1L));
    }

    @Test
    void getPostById_ShouldReturnPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Post result = postService.getPostById(1L);

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
    }

    @Test
    void getPostById_ShouldThrowWhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.getPostById(1L));
    }

    @Test
    void getPosts_ShouldReturnPostsFromCache() {
        when(cacheService.get("posts:category:technology")).thenReturn(List.of(testPost));

        List<Post> result = postService.getPosts("technology", null);

        assertEquals(1, result.size());
        verify(postRepository, never()).findAllByCategoryName(anyString());
    }

    @Test
    void getPosts_ShouldFetchByCategoryAndAuthor() {
        when(cacheService.get("posts:category:technology:author:testuser")).thenReturn(null);
        when(postRepository.findAllByCategoryNameAndAuthorUsername("technology", "testuser"))
                .thenReturn(List.of(testPost));

        List<Post> result = postService.getPosts("technology", "testuser");

        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getTitle());
        verify(cacheService).put("posts:category:technology:author:testuser", List.of(testPost));
    }

    @Test
    void getPosts_ShouldReturnAllPostsWhenNoFilters() {
        when(cacheService.get("posts:")).thenReturn(null);
        when(postRepository.findAll()).thenReturn(List.of(testPost));

        List<Post> result = postService.getPosts(null, null);

        assertEquals(1, result.size());
        verify(cacheService).put("posts:", List.of(testPost));
    }

    @Test
    void getPosts_ShouldHandleEmptyDbResult() {
        when(cacheService.get("posts:category:technology")).thenReturn(null);
        when(postRepository.findAllByCategoryName("technology")).thenReturn(Collections.emptyList());

        List<Post> result = postService.getPosts("technology", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cacheService).put("posts:category:technology", Collections.emptyList());
    }

    @Test
    void addCategoryToPost_ShouldThrowWhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.addCategoryToPost(1L, 2L));
    }

    @Test
    void addCategoryToPost_ShouldThrowWhenCategoryNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                postService.addCategoryToPost(1L, 2L));
    }
}