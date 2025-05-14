package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreatePostRequest;
import com.example.personalblog.dto.UpdatePostRequest;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import com.example.personalblog.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {
    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found";
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String POST_NOT_FOUND_MSG = "Post not found";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService = new CacheService();

    @Autowired
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Post createPost(Long id, CreatePostRequest createPostRequest) {
        Post post = new Post();

        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setAuthor(userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)));

        Set<Category> categories = Optional.ofNullable(
                createPostRequest.getCategoryNames())
                .orElse(Collections.emptyList())
                .stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(name.trim());
                            return categoryRepository.save(newCategory);
                        }))
                .collect(Collectors.toSet());

        post.setCategories(categories);
        cacheService.invalidateByPrefix("posts:");
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, UpdatePostRequest updatePostRequest) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, POST_NOT_FOUND_MSG));

        post.setTitle(updatePostRequest.getTitle());
        post.setContent(updatePostRequest.getContent());

        Set<Category> categories = Optional.ofNullable(updatePostRequest.getCategoryNames())
                .orElse(Collections.emptyList())
                .stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(name.trim());
                            return categoryRepository.save(newCategory);
                        }))
                .collect(Collectors.toSet());

        post.setCategories(categories);
        cacheService.invalidateByPrefix("posts:");
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, POST_NOT_FOUND_MSG));
        cacheService.invalidateByPrefix("posts:");
        postRepository.delete(post);
    }

    @Transactional
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, POST_NOT_FOUND_MSG));
    }

    public List<Post> getPosts(String category, String author) {
        String cacheKey = buildCacheKey(category, author);
        List<Post> cached = (List<Post>) cacheService.get(cacheKey);

        if (cached != null) {
            return cached;
        }

        List<Post> posts = fetchPostsFromDb(category, author);

        cacheService.put(cacheKey, posts);
        return posts;
    }

    private List<Post> fetchPostsFromDb(String category, String author) {
        if (category != null && author != null) {
            return postRepository.findAllByCategoryNameAndAuthorUsername(category, author);
        } else if (category != null) {
            return postRepository.findAllByCategoryName(category);
        } else if (author != null) {
            return postRepository.findAllByAuthorUsername(author);
        }
        return postRepository.findAll();
    }

    private String buildCacheKey(String category, String author) {
        StringBuilder key = new StringBuilder("posts:");
        if (category != null) {
            key.append("category:").append(category.toLowerCase());
        }
        if (author != null) {
            key.append(":author:").append(author.toLowerCase());
        }
        return key.toString();
    }

    @Transactional
    public Post addCategoryToPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, POST_NOT_FOUND_MSG));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, CATEGORY_NOT_FOUND_MSG));

        post.getCategories().add(category);
        cacheService.invalidateByPrefix("posts:");
        return postRepository.save(post);
    }
}