package com.example.personalblog.controller;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateUserRequest;
import com.example.personalblog.dto.UpdateUserRequest;
import com.example.personalblog.dto.UserDto;
import com.example.personalblog.model.User;
import com.example.personalblog.service.UserService;
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CacheService cacheService;

    @Autowired
    public UserController(UserService userService, CacheService cacheService) {
        this.userService = userService;
        this.cacheService = cacheService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
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

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(name = "withCategory", required = false) String categoryName
    ) {
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

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @Valid  @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(id, updateUserRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}