package com.example.personalblog.dto;

import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private List<String> categoryNames;

    public static PostDto fromEntity(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());
        postDto.setAuthorName(post.getAuthor().getUsername());

        List<String> categoryNames = post.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        postDto.setCategoryNames(categoryNames);
        return postDto;
    }
}
