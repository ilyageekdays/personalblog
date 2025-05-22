package com.example.personalblog.controller;

import com.example.personalblog.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visits")
@Tag(name = "Visit Counter API", description = "Управление счетчиком посещений URL")
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    @GetMapping("/count")
    @Operation(
            summary = "Получить количество посещений",
            description = "Возвращает количество посещений для указанного URL"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение счетчика",
                    content = @Content(schema = @Schema(implementation = Integer.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверный параметр запроса"
            )
    })
    public int getVisitCount(
            @Parameter(
                    description = "URL для получения счетчика",
                    required = true,
                    example = "/api/posts/1"
            )
            @RequestParam String url
    ) {
        return visitCounterService.getVisits(url);
    }

    @GetMapping("/all")
    @Operation(
            summary = "Получить все счетчики посещений",
            description = "Возвращает полную статистику посещений всех URL"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешное получение статистики",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    public Map<String, AtomicInteger> getAllVisitCounts() {
        return visitCounterService.getAllVisits();
    }
}