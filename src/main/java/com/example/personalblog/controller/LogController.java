package com.example.personalblog.controller;

import com.example.personalblog.dto.ErrorResponse;
import com.example.personalblog.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Slf4j
@Tag(name = "Логи", description = "API для получения и скачивания логов приложения")
public class LogController {

    private static final String LOG_PATH = "logs";
    private static final String LOG_FILE_PREFIX = "personal-blog-";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String MAIN_LOG_FILE = LOG_PATH + "/personal-blog.log";

    @Operation(summary = "Получить логи по дате",
            description = "Получает записи логов из файла логов приложения за указанную дату.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Логи успешно получены",
                    content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Неверный формат даты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Файл лога не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<String>> getLogsByDate(
            @Parameter(description = "Дата, за которую нужно получить логи (формат: yyyy-MM-dd)")
            @RequestParam("date") String date) {

        LocalDate logDate = parseDate(date);
        List<String> lines = new ArrayList<>();

        if (logDate.equals(LocalDate.now())) {
            File mainFile = new File(MAIN_LOG_FILE);
            if (mainFile.exists()) {
                lines.addAll(readLinesForDate(mainFile, logDate));
            }
        } else {
            File mainFile = new File(MAIN_LOG_FILE);
            File archivedFile = new File(LOG_PATH + "/"
                    + LOG_FILE_PREFIX + logDate + LOG_FILE_EXTENSION);

            if (mainFile.exists()) {
                lines.addAll(readLinesForDate(mainFile, logDate));
            }
            if (archivedFile.exists()) {
                lines.addAll(readLinesForDate(archivedFile, logDate));
            }
        }

        if (lines.isEmpty()) {
            log.warn("No log entries found for date: {}", date);
            throw new ResourceNotFoundException("No log entries found for date: " + date);
        }

        log.info("Retrieved {} log entries for date: {}", lines.size(), date);
        return ResponseEntity.ok(lines);
    }

    @Operation(summary = "Получить логи по дате",
            description = "Получает записи логов из файла логов приложения за указанную дату.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Логи успешно получены",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Неверный формат даты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл лога не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Дата, за которую нужно скачать "
                    + "файл лога (формат: yyyy-MM-dd)")
            @RequestParam("date") String date) {

        LocalDate logDate = parseDate(date);
        List<String> logEntries = new ArrayList<>();

        if (logDate.equals(LocalDate.now())) {
            File mainFile = new File(MAIN_LOG_FILE);
            if (mainFile.exists()) {
                logEntries.addAll(readLinesForDate(mainFile, logDate));
            }
        } else {
            File mainFile = new File(MAIN_LOG_FILE);
            File archivedFile = new File(LOG_PATH + "/"
                    + LOG_FILE_PREFIX + logDate + LOG_FILE_EXTENSION);

            if (mainFile.exists()) {
                logEntries.addAll(readLinesForDate(mainFile, logDate));
            }
            if (archivedFile.exists()) {
                logEntries.addAll(readLinesForDate(archivedFile, logDate));
            }
        }

        if (logEntries.isEmpty()) {
            log.warn("No log entries found for date: {}", date);
            throw new ResourceNotFoundException("No log entries found for date: " + date);
        }

        File tmp;
        try {
            tmp = Files.createTempFile("log-" + logDate + "-", ".log").toFile();
            try (FileWriter writer = new FileWriter(tmp)) {
                for (String entry : logEntries) {
                    writer.write(entry + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            log.error("Error preparing temporary file for date: {}", date, e);
            throw new RuntimeException("Error preparing log file: " + e.getMessage());
        }

        Resource resource = new FileSystemResource(tmp);
        String headerValue = "attachment; filename=\"log(" + logDate + ").log\"";
        log.info("Downloading log file for date: {}", date);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    /** Parse date with human-readable error message. */
    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (java.time.format.DateTimeParseException ex) {
            log.warn("Invalid date format: {}", dateString, ex);
            throw new IllegalArgumentException("Invalid date format: '" + dateString + "'. Please use yyyy-MM-dd", ex);
        }
    }

    /** Reads lines related to a specific date from the specified source file. */
    private List<String> readLinesForDate(File source, LocalDate date) {
        List<String> out = new ArrayList<>();
        String datePrefix = date.toString();
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(datePrefix)) {
                    out.add(line);
                }
            }
        } catch (IOException ex) {
            log.error("Error reading log file: {}", source.getAbsolutePath(), ex);
            throw new RuntimeException("Error reading log file: " + ex.getMessage());
        }
        return out;
    }
}