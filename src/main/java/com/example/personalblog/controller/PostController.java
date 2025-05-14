package com.example.personalblog.controller;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreatePostRequest;
import com.example.personalblog.dto.PostDto;
import com.example.personalblog.dto.UpdatePostRequest;
import com.example.personalblog.model.Post;
import com.example.personalblog.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService, CacheService cacheService) {
        this.postService = postService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequest createPostRequest,
            @PathVariable Long userId) {
        Post post = postService.createPost(userId, createPostRequest);
        PostDto postDto = PostDto.fromEntity(post);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId())
                .toUri();

        return ResponseEntity.created(location).body(postDto);
    }

    @GetMapping
    public ResponseEntity<List<?>> getPosts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "author", required = false) String author
    ) {
        List<Post> posts = postService.getPosts(category, author);
        List<PostDto> postsDto = posts.stream().map(PostDto::fromEntity).toList();
        return posts.isEmpty()
                ? ResponseEntity.noContent().build()
                : (category == null)
                ? ResponseEntity.ok(postsDto)
                : ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public PostDto getPostById(@PathVariable Long id) {
        return PostDto.fromEntity(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @Valid  @RequestBody UpdatePostRequest updatePostRequest) {
        Post post = postService.updatePost(id, updatePostRequest);
        PostDto postDto = PostDto.fromEntity(post);
        return ResponseEntity.ok(postDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/categories/{categoryId}")
    public ResponseEntity<PostDto> addCategoryToPost(
            @PathVariable Long postId,
            @PathVariable Long categoryId) {
        Post post = postService.addCategoryToPost(postId, categoryId);
        PostDto postDto = PostDto.fromEntity(post);
        return ResponseEntity.ok(postDto);
    }
}