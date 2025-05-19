package com.example.personalblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для стандартизированного ответа об ошибке.
 * Используется для возвращения структурированной информации об ошибках API.
 */
@NoArgsConstructor
@Data
@Schema(description = "Стандартизированный ответ об ошибке API")
public class ErrorResponse {

    @Schema(
            description = "HTTP статус код ошибки",
            example = "400",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int status;

    @Schema(
            description = "Тип ошибки",
            example = "Bad Request",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String error;

    @Schema(
            description = "Подробное сообщение об ошибке",
            example = "Поле 'email' должно быть валидным email адресом",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String message;

    @Schema(
            description = "Временная метка возникновения ошибки",
            example = "2023-11-15T14:30:45.12345",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Конструктор для удобного создания объекта ошибки.
     *
     * @param status HTTP статус код
     *
     * @param error Тип ошибки
     *
     * @param message Подробное сообщение
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}