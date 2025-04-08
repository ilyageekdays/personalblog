package com.example.personalblog.service;

import com.example.personalblog.model.BlogPost;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;



@Service
public class BlogPostsService {
    private final Map<Integer, BlogPost> postsDatabase = Map.of(
            1, new BlogPost("First post", 1),
            2, new BlogPost("Cool story post", 2),
            3, new BlogPost("Weekend post", 3),
            4, new BlogPost("Christmas", 4)
    );

    public BlogPost createBlogPost(String name, Integer id) {
        return new BlogPost(name, id);
    }

    public BlogPost getPostById(Integer id) {
        if (!postsDatabase.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return postsDatabase.get(id);
    }

    public BlogPost getPostByName(String name) {
        return postsDatabase.values().stream()
                .filter(post -> post.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Post not found"));
    }
}