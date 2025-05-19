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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       CategoryRepository categoryRepository, CacheService cacheService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.cacheService = cacheService;
    }

    public Post createPost(Long userId, CreatePostRequest request) {
        if (request == null) {
            throw new NullPointerException("CreatePostRequest cannot be null");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);

        Set<Category> categories = Optional.ofNullable(request.getCategoryNames())
                .orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(name);
                            return categoryRepository.save(newCategory);
                        }))
                .collect(Collectors.toSet());

        post.setCategories(categories);
        Post savedPost = postRepository.save(post);
        cacheService.invalidateByPrefix("posts:");
        return savedPost;
    }

    public List<Post> createPostsBulk(Long userId, List<CreatePostRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Post> posts = requests.stream().map(request -> {
            Post post = new Post();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setAuthor(author);

            Set<Category> categories = Optional.ofNullable(request.getCategoryNames())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(String::trim)
                    .map(name -> categoryRepository.findByName(name)
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(name);
                                return categoryRepository.save(newCategory);
                            }))
                    .collect(Collectors.toSet());

            post.setCategories(categories);
            return post;
        }).toList();

        List<Post> savedPosts = postRepository.saveAll(posts);
        cacheService.invalidateByPrefix("posts:");
        return savedPosts;
    }

    public Post updatePost(Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        Set<Category> categories = Optional.ofNullable(request.getCategoryNames())
                .orElse(Collections.emptyList())
                .stream()
                .map(String::trim)
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(name);
                            return categoryRepository.save(newCategory);
                        }))
                .collect(Collectors.toSet());

        post.setCategories(categories);
        Post savedPost = postRepository.save(post);
        cacheService.invalidateByPrefix("posts:");
        return savedPost;
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        postRepository.delete(post);
        cacheService.invalidateByPrefix("posts:");
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    public List<Post> getPosts(String category, String author) {
        String cacheKey = "posts:";
        if (category != null && author != null) {
            cacheKey += "category:" + category + ":author:" + author;
        } else if (category != null) {
            cacheKey += "category:" + category;
        } else if (author != null) {
            cacheKey += "author:" + author;
        }

        List<Post> cachedPosts = (List<Post>) cacheService.get(cacheKey);
        if (cachedPosts != null) {
            return cachedPosts;
        }

        List<Post> posts;
        if (category != null && author != null) {
            posts = postRepository.findAllByCategoryNameAndAuthorUsername(category, author);
        } else if (category != null) {
            posts = postRepository.findAllByCategoryName(category);
        } else if (author != null) {
            posts = postRepository.findAllByAuthorUsername(author);
        } else {
            posts = postRepository.findAll();
        }

        cacheService.put(cacheKey, posts);
        return posts;
    }

    public Post addCategoryToPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        boolean categoryAdded = post.getCategories().add(category);
        if (categoryAdded) {
            postRepository.save(post);
        }
        cacheService.invalidateByPrefix("posts:");
        return post;
    }
}