package com.example.personalblog.dto;

import com.example.personalblog.model.Category;
import com.example.personalblog.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * DTO для представления поста блога.
 * Содержит основную информацию о посте, включая автора и связанные категории.
 */
@Data
@Schema(description = "DTO для представления поста блога")
public class PostDto {

    @Schema(
            description = "Уникальный идентификатор поста",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Заголовок поста",
            example = "Основы Spring Boot",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 200
    )
    private String title;

    @Schema(
            description = "Содержание поста",
            example = "Spring Boot упрощает разработку Spring-приложений...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Schema(
            description = "Дата и время создания поста",
            example = "2023-11-15T14:30:45",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Дата и время последнего обновления поста",
            example = "2023-11-16T10:15:30",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Имя автора поста",
            example = "john_doe",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String authorName;

    @Schema(
            description = "Список названий категорий поста",
            example = "[\"Программирование\", \"Java\"]",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<String> categoryNames;

    /**
     * Преобразует сущность Post в PostDto.
     *
     * @param post Сущность поста
     * @return DTO поста
     */
    public static PostDto fromEntity(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());
        postDto.setAuthorName(post.getAuthor().getUsername());

        if (post.getCategories() != null) {
            List<String> categoryNames = post.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            postDto.setCategoryNames(categoryNames);
        }

        return postDto;
    }
}