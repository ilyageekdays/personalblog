package com.example.personalblog.service;

import com.example.personalblog.dto.LogFileInfo;
import com.example.personalblog.dto.LogStatusResponse;
import com.example.personalblog.dto.LogTaskInfo;
import com.example.personalblog.exception.ResourceNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncLogService {

    private static final String LOG_PATH = "logs";
    private static final String LOG_FILE_PREFIX = "personal-blog-";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String MAIN_LOG_FILE = LOG_PATH + "/personal-blog.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ConcurrentHashMap<String, LogTaskInfo> logTasks = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> createLogFile(String date) {
        String logId = UUID.randomUUID().toString();
        LocalDate dateObj = LocalDate.parse(date, DATE_FORMATTER);
        String fileName = buildLogFilePath(dateObj);
        logTasks.put(logId, new LogTaskInfo("IN_PROGRESS", fileName, date));

        CompletableFuture.runAsync(() -> {
            try {
                // Имитация долгой операции
                Thread.sleep(20000);
                LocalDate logDate = parseDate(date);
                createLogFileIfNotExists(logDate, fileName);
                logTasks.get(logId).setStatus("COMPLETED");
            } catch (Exception e) {
                logTasks.get(logId).setStatus("FAILED");
                log.error("Log creation failed", e);
            }
        });
        return CompletableFuture.completedFuture(logId);
    }

    public LogStatusResponse getLogStatus(String logId) {
        LogTaskInfo taskInfo = logTasks.get(logId);
        if (taskInfo == null) {
            return new LogStatusResponse(logId, "NOT_FOUND", null);
        }

        // Дополнительная проверка существования файла
        if ("COMPLETED".equals(taskInfo.getStatus())) {
            File logFile = new File(taskInfo.getFilePath());
            if (!logFile.exists()) {
                taskInfo.setStatus("FAILED");
            }
        }

        return new LogStatusResponse(
                logId,
                taskInfo.getStatus(),
                taskInfo.getDate()
        );
    }

    public LogFileInfo getLogFile(String logId) throws IOException {
        LogTaskInfo taskInfo = logTasks.get(logId);
        if (taskInfo == null) {
            throw new ResourceNotFoundException("Log task not found");
        }

        File logFile = new File(taskInfo.getFilePath());
        if (!logFile.exists() || logFile.length() == 0) {
            throw new ResourceNotFoundException("Log file not found or empty");
        }

        return new LogFileInfo(logFile, taskInfo.getDate());
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    private String buildLogFilePath(LocalDate date) {
        return LOG_PATH + "/" + LOG_FILE_PREFIX + date + LOG_FILE_EXTENSION;
    }

    private void createLogFileIfNotExists(LocalDate date, String fileName)
            throws InterruptedException, IOException {
        File logFile = new File(fileName);
        if (!logFile.exists()) {
            ensureLogDirectoryExists();
            writeLogEntriesToFile(date, logFile);
        }
    }

    private void ensureLogDirectoryExists() throws IOException {
        File logDir = new File(LOG_PATH);
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new IOException("Failed to create log directory");
        }
    }

    private void writeLogEntriesToFile(LocalDate date, File logFile) throws IOException {
        try (FileWriter writer = new FileWriter(logFile)) {
            for (String entry : collectLogEntries(date)) {
                writer.write(entry + System.lineSeparator());
            }
        }
    }

    private List<String> collectLogEntries(LocalDate date) throws IOException {
        List<String> entries = new ArrayList<>();
        String datePrefix = date.toString();

        collectEntriesFromFile(new File(MAIN_LOG_FILE), datePrefix, entries);
        collectEntriesFromFile(new File(buildLogFilePath(date)), datePrefix, entries);

        return entries;
    }

    private void collectEntriesFromFile(File file, String datePrefix, List<String> entries)
            throws IOException {
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(datePrefix)) {
                        entries.add(line);
                    }
                }
            }
        }
    }
}