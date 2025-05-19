package com.example.personalblog.controller;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CategoryDto;
import com.example.personalblog.dto.CreateCategoryRequest;
import com.example.personalblog.model.Category;
import com.example.personalblog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Контроллер для управления категориями блога.
 * Предоставляет CRUD-операции для работы с категориями.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category API", description = "Управление категориями блога")
public class CategoryController {

    private final CategoryService categoryService;
    private final CacheService cacheService;

    public CategoryController(CategoryService categoryService, CacheService cacheService) {
        this.categoryService = categoryService;
        this.cacheService = cacheService;
    }

    /**
     * Создает новую категорию.
     *
     * @param createCategoryRequest DTO с данными для создания категории
     * @return ResponseEntity с созданной категорией и HTTP-статусом 201 (Created)
     */
    @PostMapping
    @Operation(
            summary = "Создать категорию",
            description = "Создает новую категорию блога"
    )
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Категория успешно создана",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            ), @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            )
    })
    public ResponseEntity<CategoryDto> createCategory(
            @RequestBody CreateCategoryRequest createCategoryRequest) {
        Category category = categoryService.createCategory(createCategoryRequest);
        CategoryDto categoryDto = CategoryDto.fromEntity(category);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(categoryDto);
    }

    /**
     * Получает список всех категорий.
     *
     * @return ResponseEntity со списком категорий или HTTP-статусом 204 (No Content)
     */
    @GetMapping
    @Operation(
            summary = "Получить все категории",
            description = "Возвращает список всех категорий блога"
    )
    @ApiResponses({@ApiResponse(
                    responseCode = "200",
                    description = "Список категорий успешно получен",
                    content = @Content(schema = @Schema(implementation = Category.class))
            ), @ApiResponse(
                    responseCode = "204",
                    description = "Категории не найдены"
            )
    })
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(categories);
        }
    }

    /**
     * Получает категорию по ID.
     *
     * @param id ID категории
     * @return DTO категории
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить категорию по ID",
            description = "Возвращает категорию по указанному идентификатору"
    )
    @ApiResponses({@ApiResponse(
                    responseCode = "200",
                    description = "Категория найдена",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            ), @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена"
            )
    })
    public CategoryDto getCategoryById(
            @Parameter(description = "ID категории", example = "1")
            @PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return CategoryDto.fromEntity(category);
    }

    /**
     * Обновляет существующую категорию.
     *
     * @param id ID категории для обновления
     * @param categoryDetails Обновленные данные категории
     * @return DTO обновленной категории
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить категорию",
            description = "Обновляет данные существующей категории"
    )
    @ApiResponses({@ApiResponse(
                    responseCode = "200",
                    description = "Категория успешно обновлена",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            ), @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена"
            )
    })
    public CategoryDto updateCategory(
            @Parameter(description = "ID категории", example = "1")
            @PathVariable Long id,
            @RequestBody Category categoryDetails) {
        Category category = categoryService.updateCategory(id, categoryDetails);
        return CategoryDto.fromEntity(category);
    }

    /**
     * Удаляет категорию по ID.
     *
     * @param id ID категории для удаления
     * @return ResponseEntity с HTTP-статусом 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить категорию",
            description = "Удаляет категорию по указанному идентификатору"
    )
    @ApiResponses({@ApiResponse(
                    responseCode = "204",
                    description = "Категория успешно удалена"
            ), @ApiResponse(
                    responseCode = "404",
                    description = "Категория не найдена"
            )
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории", example = "1")
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}