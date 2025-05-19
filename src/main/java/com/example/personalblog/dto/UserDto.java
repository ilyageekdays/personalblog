package com.example.personalblog.dto;

import com.example.personalblog.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO для представления пользователя.
 * Содержит основные данные пользователя без чувствительной информации.
 */
@Data
@Schema(description = "DTO для представления данных пользователя")
public class UserDto {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Отображаемое имя пользователя",
            example = "Иван Иванов",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String visibleName;

    @Schema(
            description = "Уникальное имя пользователя (логин)",
            example = "ivan_2023",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String username;

    @Schema(
            description = "Email пользователя",
            example = "user@example.com",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "email"
    )
    private String email;

    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user Сущность пользователя
     * @return DTO пользователя
     */
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setVisibleName(user.getVisibleName());
        return userDto;
    }
}