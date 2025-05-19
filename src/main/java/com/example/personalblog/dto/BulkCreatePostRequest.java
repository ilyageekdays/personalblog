package com.example.personalblog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BulkCreatePostRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotEmpty(message = "Posts list cannot be empty")
    private List<@Valid CreatePostRequest> posts;

}