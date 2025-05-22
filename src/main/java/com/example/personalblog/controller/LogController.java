package com.example.personalblog.controller;

import com.example.personalblog.dto.ErrorResponse;
import com.example.personalblog.dto.LogCreateResponse;
import com.example.personalblog.dto.LogStatusResponse;
import com.example.personalblog.service.AsyncLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Log API", description = "API для управления лог-файлами приложения")
public class LogController {

    private final AsyncLogService asyncLogService;

    @GetMapping("/create")
    @Operation(
            summary = "Запустить создание лог-файла",
            description = "Инициирует асинхронное создание лог-файла для указанной даты"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Задача создания лога успешно запущена",
                    content = @Content(schema = @Schema(implementation = LogCreateResponse.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат даты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public CompletableFuture<ResponseEntity<LogCreateResponse>> createLogFile(
            @Parameter(
                    description = "Дата в формате yyyy-MM-dd",
                    required = true,
                    example = "2024-01-01"
            )
            @RequestParam String date) throws InterruptedException {

        log.info("Creating log file for date: {}", date);
        return asyncLogService.createLogFile(date)
                .thenApply(LogCreateResponse::new)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{logId}/status")
    @Operation(
            summary = "Получить статус задачи создания лога",
            description = "Возвращает текущий статус задачи по созданию лог-файла"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Статус задачи успешно получен",
                    content = @Content(schema = @Schema(implementation = LogStatusResponse.class))
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Задача с указанным ID не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<LogStatusResponse> getLogStatus(
            @Parameter(
                    description = "ID задачи создания лога",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String logId,

            @Parameter(
                    description = "Дата в формате yyyy-MM-dd",
                    required = true,
                    example = "2024-01-01"
            )
            @RequestParam String date) throws InterruptedException {

        log.info("Checking status for log ID: {} and date: {}", logId, date);
        return ResponseEntity.ok(asyncLogService.getLogStatus(logId, date));
    }

    @GetMapping("/download")
    @Operation(
            summary = "Скачать лог-файл",
            description = "Возвращает лог-файл для указанной даты в виде файлового ответа"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Лог-файл успешно скачан",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Лог-файл не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
        @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(
                    description = "Дата в формате yyyy-MM-dd",
                    required = true,
                    example = "2024-01-01"
            )
            @RequestParam String date) throws InterruptedException, IOException {

        log.info("Downloading log file for date: {}", date);
        try {
            File logFile = asyncLogService.getLogFile(date);
            return buildFileResponse(logFile, date);
        } catch (InterruptedException e) {
            log.error("Error downloading log file for date: {}", date, e);
            throw new InterruptedException();
        } catch (IOException e) {
            log.error("Error file is not for date: {}", date, e);
            throw new IOException();
        }
    }

    private ResponseEntity<Resource> buildFileResponse(File file, String date) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDispositionHeader(date))
                .body(new FileSystemResource(file));
    }

    private String buildContentDispositionHeader(String date) {
        return "attachment; filename=\"log_" + date + ".log\"";
    }
}