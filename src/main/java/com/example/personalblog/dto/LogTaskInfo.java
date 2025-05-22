package com.example.personalblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogTaskInfo {
    private String status;
    private String filePath;
    private String date;
}