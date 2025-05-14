package com.example.personalblog.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    private List<String> categoryNames;
}