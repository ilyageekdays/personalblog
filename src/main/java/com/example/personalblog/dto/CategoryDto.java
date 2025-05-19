package com.example.personalblog.dto;

import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * DTO для представления категории.
 * Содержит основную информацию о категории и список названий связанных постов.
 */
@Data
@Schema(description = "DTO категории блога")
public class CategoryDto {

    @Schema(
            description = "Уникальный идентификатор категории",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Название категории",
            example = "Программирование",
            required = true,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Список названий постов в этой категории",
            example = "[\"Spring Boot Basics\", \"REST API Design\"]",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> postsTitles;

    /**
     * Преобразует сущность Category в CategoryDto.
     *
     * @param category Сущность категории
     * @return DTO категории
     */
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