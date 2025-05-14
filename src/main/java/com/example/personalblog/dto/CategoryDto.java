package com.example.personalblog.dto;

import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private List<String> postsTitles;

    public static CategoryDto fromEntity(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        if (category.getPosts() != null) {
            categoryDto.setPostsTitles(
                    category.getPosts().stream()
                            .map(Post::getTitle)
                            .collect(Collectors.toList())
            );
        }

        return categoryDto;
    }
}