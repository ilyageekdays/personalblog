package com.example.personalblog.controller;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateUserRequest;
import com.example.personalblog.dto.UpdateUserRequest;
import com.example.personalblog.dto.UserDto;
import com.example.personalblog.model.User;
import com.example.personalblog.service.UserService;
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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Контроллер для управления пользователями блога.
 * Предоставляет CRUD-операции для работы с пользователями,
 * включая поиск по категориям постов.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Управление пользователями блога")
public class UserController {

    private final UserService userService;
    private final CacheService cacheService;

    @Autowired
    public UserController(UserService userService, CacheService cacheService) {
        this.userService = userService;
        this.cacheService = cacheService;
    }

    /**
     * Создает нового пользователя.
     *
     * @param createUserRequest DTO с данными для создания пользователя
     * @return ResponseEntity с созданным пользователем и HTTP-статусом 201 (Created)
     */
    @PostMapping
    @Operation(
            summary = "Создать пользователя",
            description = "Регистрирует нового пользователя в системе"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
        @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
        @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email/логином уже существует"
            )
    })
    public ResponseEntity<UserDto> createUser(
            @Parameter(description = "Данные для создания пользователя", required = true)
            @Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        UserDto userDto = UserDto.fromEntity(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location).body(userDto);
    }

    /**
     * Получает список пользователей с возможностью фильтрации по категории постов.
     *
     * @param categoryName Название категории для фильтрации (опционально)
     * @return ResponseEntity со списком пользователей или HTTP-статусом 204 (No Content)
     */
    @GetMapping
    @Operation(
            summary = "Получить пользователей",
            description = "Возвращает список пользователей "
                    + "с возможностью фильтрации по категории их постов"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
        @ApiResponse(
                    responseCode = "204",
                    description = "Пользователи не найдены"
            )
    })
    public ResponseEntity<List<UserDto>> getUsers(
            @Parameter(description = "Название категории для фильтрации", example = "technology")
            @RequestParam(name = "withCategory", required = false) String categoryName) {
        List<UserDto> usersDto = (categoryName != null)
                ? userService.findUsersByPostCategory(categoryName).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList())
                : userService.getAllUsers().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());

        return usersDto.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(usersDto);
    }

    /**
     * Получает пользователя по ID.
     *
     * @param id ID пользователя
     * @return Сущность пользователя
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает полные данные пользователя по указанному идентификатору"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    public User getUserById(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param id ID пользователя для обновления
     * @param updateUserRequest Обновленные данные пользователя
     * @return Обновленная сущность пользователя
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные существующего пользователя"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = User.class))
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
    public User updateUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Данные для обновления пользователя", required = true)
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(id, updateUserRequest);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param id ID пользователя для удаления
     * @return ResponseEntity с HTTP-статусом 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по указанному идентификатору"
    )
    @ApiResponses({
        @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно удален"
            ),
        @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}