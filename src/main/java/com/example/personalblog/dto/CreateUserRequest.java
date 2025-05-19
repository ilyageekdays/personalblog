package com.example.personalblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса на создание пользователя.
 * Содержит данные, необходимые для регистрации нового пользователя.
 */
@Data
@Schema(description = "DTO запроса на создание пользователя")
public class CreateUserRequest {

    @NotBlank(message = "Отображаемое имя обязательно")
    @Size(min = 2, max = 50, message = "Отображаемое имя должно быть от 2 до 50 символов")
    @Schema(
            description = "Имя, которое будет отображаться в системе",
            example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 50
    )
    private String visibleName;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 20, message = "Имя пользователя должно быть от 3 до 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Имя пользователя может содержать только буквы, цифры и подчеркивания")
    @Schema(
            description = "Уникальное имя пользователя (логин)",
            example = "ivan_2023",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 20,
            pattern = "^[a-zA-Z0-9_]+$"
    )
    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(
            description = "Email пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "email"
    )
    private String email;
}