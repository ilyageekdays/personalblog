package com.example.personalblog.controller;

import com.example.personalblog.model.Post;
import com.example.personalblog.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/user/{userId}")
    public Post createPost(@RequestBody Post post, @PathVariable Long userId) {
        return postService.createPost(post, userId);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post postDetails) {
        return postService.updatePost(id, postDetails);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    @PostMapping("/{postId}/categories/{categoryId}")
    public Post addCategoryToPost(@PathVariable Long postId, @PathVariable Long categoryId) {
        return postService.addCategoryToPost(postId, categoryId);
    }
}