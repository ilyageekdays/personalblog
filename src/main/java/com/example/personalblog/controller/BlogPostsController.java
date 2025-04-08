package com.example.personalblog.controller;

import com.example.personalblog.model.BlogPost;
import com.example.personalblog.service.BlogPostsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BlogPostsController {

    private final BlogPostsService postsService;

    public BlogPostsController(BlogPostsService postsService) {
        this.postsService = postsService;
    }

    @GetMapping("/post")
    public BlogPost getQueryPost(@RequestParam("name") String name) {
        return postsService.getPostByName(name);
    }

    @GetMapping("/post/{postId}")
    public BlogPost getPathPost(@PathVariable("postId") Integer postId) {
        return postsService.getPostById(postId);
    }
}