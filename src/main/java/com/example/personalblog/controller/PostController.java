package com.example.personalblog.controller;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.BulkCreatePostRequest;
import com.example.personalblog.dto.CreatePostRequest;
import com.example.personalblog.dto.PostDto;
import com.example.personalblog.dto.UpdatePostRequest;
import com.example.personalblog.model.Post;
import com.example.personalblog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Контроллер для управления постами блога.
 * Предоставляет CRUD-операции для работы с постами,
 * включая привязку категорий к постам.
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post API", description = "Управление постами блога")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService, CacheService cacheService) {
        this.postService = postService;
    }

    /**
     * Создает новый пост для указанного пользователя.
     *
     * @param createPostRequest DTO с данными для создания поста
     * @param userId ID пользователя-автора
     * @return ResponseEntity с созданным постом и HTTP-статусом 201 (Created)
     */
    @PostMapping("/user/{userId}")
    @Operation(
            summary = "Создать пост",
            description = "Создает новый пост для указанного пользователя"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "201",
                    description = "Пост успешно создан",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    public ResponseEntity<PostDto> createPost(
            @Parameter(description = "Данные для создания поста", required = true)
            @Valid @RequestBody CreatePostRequest createPostRequest,

            @Parameter(description = "ID пользователя-автора", example = "1")
            @PathVariable Long userId) {
        Post post = postService.createPost(userId, createPostRequest);
        PostDto postDto = PostDto.fromEntity(post);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId())
                .toUri();

        return ResponseEntity.created(location).body(postDto);
    }

    /**
     * Получает список постов с возможностью фильтрации по категории и автору.
     *
     * @param category Название категории для фильтрации (опционально)
     * @param author Имя автора для фильтрации (опционально)
     * @return ResponseEntity со списком постов или HTTP-статусом 204 (No Content)
     */
    @GetMapping
    @Operation(
            summary = "Получить посты",
            description = "Возвращает список постов с возможностью фильтрации по категории и автору"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Список постов успешно получен",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "204",
                    description = "Посты не найдены"
            )
    })
    public ResponseEntity<List<?>> getPosts(
            @Parameter(description = "Название категории для фильтрации", example = "technology")
            @RequestParam(name = "category", required = false) String category,

            @Parameter(description = "Имя автора для фильтрации", example = "john_doe")
            @RequestParam(name = "author", required = false) String author) {
        List<Post> posts = postService.getPosts(category, author);
        List<PostDto> postsDto = posts.stream().map(PostDto::fromEntity).toList();
        return posts.isEmpty()
                ? ResponseEntity.noContent().build()
                : (category == null)
                ? ResponseEntity.ok(postsDto)
                : ResponseEntity.ok(posts);
    }

    /**
     * Получает пост по ID.
     *
     * @param id ID поста
     * @return DTO поста
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пост по ID",
            description = "Возвращает пост по указанному идентификатору"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Пост найден",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пост не найден"
            )
    })
    public PostDto getPostById(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long id) {
        return PostDto.fromEntity(postService.getPostById(id));
    }

    /**
     * Обновляет существующий пост.
     *
     * @param id ID поста для обновления
     * @param updatePostRequest Обновленные данные поста
     * @return DTO обновленного поста
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить пост",
            description = "Обновляет данные существующего поста"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Пост успешно обновлен",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пост не найден"
            )
    })
    public ResponseEntity<PostDto> updatePost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Данные для обновления поста", required = true)
            @Valid @RequestBody UpdatePostRequest updatePostRequest) {
        Post post = postService.updatePost(id, updatePostRequest);
        PostDto postDto = PostDto.fromEntity(post);
        return ResponseEntity.ok(postDto);
    }

    /**
     * Удаляет пост по ID.
     *
     * @param id ID поста для удаления
     * @return ResponseEntity с HTTP-статусом 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пост",
            description = "Удаляет пост по указанному идентификатору"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "204",
                    description = "Пост успешно удален"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пост не найден"
            )
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Добавляет категорию к посту.
     *
     * @param postId ID поста
     * @param categoryId ID категории
     * @return DTO поста с обновленными категориями
     */
    @PostMapping("/{postId}/categories/{categoryId}")
    @Operation(
            summary = "Добавить категорию к посту",
            description = "Связывает указанную категорию с постом"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Категория успешно добавлена к посту",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пост или категория не найдены"
            )
    })
    public ResponseEntity<PostDto> addCategoryToPost(
            @Parameter(description = "ID поста", example = "1")
            @PathVariable Long postId,

            @Parameter(description = "ID категории", example = "2")
            @PathVariable Long categoryId) {
        Post post = postService.addCategoryToPost(postId, categoryId);
        PostDto postDto = PostDto.fromEntity(post);
        return ResponseEntity.ok(postDto);
    }

    @PostMapping("/bulk/user/{userId}")
    @Operation(
            summary = "Массовое создание постов",
            description = "Создает несколько постов для указанного пользователя за одну операцию"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "201",
                    description = "Посты успешно созданы",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    public ResponseEntity<List<PostDto>> createPostsBulk(
            @Parameter(description = "ID пользователя-автора", example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Список постов для создания", required = true)
            @Valid @RequestBody List<@Valid CreatePostRequest> createPostRequests) {

        List<Post> createdPosts = postService.createPostsBulk(userId, createPostRequests);
        List<PostDto> postDtos = createdPosts.stream()
                .map(PostDto::fromEntity)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(postDtos);
    }

    // Или альтернативный вариант с BulkCreatePostRequest
    @PostMapping("/bulk")
    @Operation(
            summary = "Массовое создание постов",
            description = "Создает несколько постов для указанного пользователя за одну операцию"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "201",
                    description = "Посты успешно созданы",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    public ResponseEntity<List<PostDto>> createPostsBulk(
            @Parameter(description = "Запрос на массовое создание постов", required = true)
            @Valid @RequestBody BulkCreatePostRequest bulkRequest) {

        List<Post> createdPosts = postService.createPostsBulk(bulkRequest.getUserId(),
                bulkRequest.getPosts());
        List<PostDto> postDtos = createdPosts.stream()
                .map(PostDto::fromEntity)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(postDtos);
    }
}