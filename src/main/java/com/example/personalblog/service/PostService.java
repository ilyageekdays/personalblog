package com.example.personalblog.service;

import com.example.personalblog.exception.NotFoundException;
import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import com.example.personalblog.model.User;
import com.example.personalblog.repository.CategoryRepository;
import com.example.personalblog.repository.PostRepository;
import com.example.personalblog.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found";
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String POST_NOT_FOUND_MSG = "Post not found";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Post createPost(Post post, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG));
        post.setAuthor(author);
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        postRepository.delete(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByAuthorId(userId);
    }

    public List<Post> getPostsByCategory(Long categoryId) {
        return postRepository.findByCategoriesId(categoryId);
    }

    @Transactional
    public Post addCategoryToPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"
                ));

        post.getCategories().add(category);
        return postRepository.save(post);
    }

    @Transactional
    public Post removeCategoryFromPost(Long postId, Long categoryId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MSG));

        post.getCategories().remove(category);
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePostCategories(Long postId, Set<Long> categoryIds) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        post.setCategories(categories);

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePostAuthor(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(POST_NOT_FOUND_MSG));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG));

        post.setAuthor(author);
        return postRepository.save(post);
    }
}