package com.example.personalblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * DTO для запроса на создание категории.
 * Содержит данные, необходимые для создания новой категории.
 */
@Data
@Schema(description = "DTO запроса на создание категории")
public class CreateCategoryRequest {

    @NotBlank(message = "Название категории обязательно")
    @Size(min = 2, max = 100, message = "Название категории должно быть от 2 до 100 символов")
    @Schema(
            description = "Название создаваемой категории",
            example = "Программирование",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Список названий связанных категорий (опционально)",
            example = "[\"Java\", \"Spring Framework\"]",
            nullable = true
    )
    private List<
            @Size(min = 2, max = 50,
                    message = "Название связанной категории должно быть от 2 до 50 символов")
                    String> categoryNames;
}