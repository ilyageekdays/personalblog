package com.example.personalblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * DTO для запроса на обновление поста.
 * Содержит данные, необходимые для обновления существующего поста.
 */
@Data
@Schema(description = "DTO запроса на обновление поста")
public class UpdatePostRequest {

    @NotBlank(message = "Заголовок поста обязателен")
    @Size(min = 5, max = 200, message = "Заголовок должен содержать от 5 до 200 символов")
    @Schema(
            description = "Новый заголовок поста",
            example = "Продвинутые техники Spring Boot",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 5,
            maxLength = 200
    )
    private String title;

    @NotBlank(message = "Содержание поста обязательно")
    @Size(min = 10, message = "Содержание должно быть не менее 10 символов")
    @Schema(
            description = "Новое содержание поста",
            example = "В этой статье мы рассмотрим продвинутые техники работы со Spring Boot...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Schema(
            description = "Обновленный список категорий поста",
            example = "[\"Программирование\", \"Spring Framework\"]",
            nullable = true
    )
    private List<
            @Size(min = 2, max = 50, message = "Название категории должно быть от 2 до 50 символов")
                    String> categoryNames;
}