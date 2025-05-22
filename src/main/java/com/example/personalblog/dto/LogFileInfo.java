package com.example.personalblog.dto;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogFileInfo {
    private File file;
    private String date;
}