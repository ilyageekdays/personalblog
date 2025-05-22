package com.example.personalblog.controller;

import com.example.personalblog.dto.LogCreateResponse;
import com.example.personalblog.dto.LogFileInfo;
import com.example.personalblog.dto.LogStatusResponse;
import com.example.personalblog.service.AsyncLogService;
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
    public CompletableFuture<ResponseEntity<LogCreateResponse>> createLogFile(
            @RequestParam String date
    ) {
        return asyncLogService.createLogFile(date)
                .thenApply(LogCreateResponse::new)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{logId}/status")
    public ResponseEntity<LogStatusResponse> getLogStatus(
            @PathVariable String logId
    ) {
        return ResponseEntity.ok(asyncLogService.getLogStatus(logId));
    }

    // Скачивание файла по logId
    @GetMapping("/{logId}/download")
    public ResponseEntity<Resource> downloadLogFile(
            @PathVariable String logId
    ) throws IOException {
        LogFileInfo logFileInfo = asyncLogService.getLogFile(logId);
        return buildFileResponse(logFileInfo.getFile(), logFileInfo.getDate());
    }

    private ResponseEntity<Resource> buildFileResponse(File file, String date) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"logs_" + date + ".log\""
                )
                .body(new FileSystemResource(file));
    }
}