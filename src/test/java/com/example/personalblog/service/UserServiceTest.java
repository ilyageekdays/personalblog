package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateUserRequest;
import com.example.personalblog.dto.UpdateUserRequest;
import com.example.personalblog.model.User;
import com.example.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserService userService;

    private User user;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setVisibleName("Test User");

        createRequest = new CreateUserRequest();
        createRequest.setUsername("testuser");
        createRequest.setEmail("test@example.com");
        createRequest.setVisibleName("Test User");

        updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setVisibleName("Updated User");
    }

    // Тесты для createUser
    @Test
    void createUser_ShouldCreateNewUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(createRequest);

        assertThat(result).isNotNull();
        verify(cacheService).invalidateByPrefix("users:");
    }

    @Test
    void createUser_ShouldThrowConflictForDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void createUser_ShouldThrowConflictForDuplicateUsername() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    // Тесты для getUserById
    @Test
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUserById_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    // Тесты для getAllUsers
    @Test
    void getAllUsers_ShouldReturnFromCache() {
        when(cacheService.get("users:")).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertThat(result).containsExactly(user);
        verify(userRepository, never()).findAll();
    }

    @Test
    void getAllUsers_ShouldFetchFromDbAndCacheWhenNotInCache() {
        when(cacheService.get("users:")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertThat(result).containsExactly(user);
        verify(cacheService).put("users:", List.of(user));
    }

    // Тесты для findUsersByPostCategory
    @Test
    void findUsersByPostCategory_ShouldReturnFromCache() {
        when(cacheService.get("users:category:test")).thenReturn(List.of(user));

        List<User> result = userService.findUsersByPostCategory("test");

        assertThat(result).containsExactly(user);
        verify(userRepository, never()).findUsersByPostCategory(any());
    }

    @Test
    void findUsersByPostCategory_ShouldFetchFromDbAndCacheWhenNotInCache() {
        when(cacheService.get("users:category:test")).thenReturn(null);
        when(userRepository.findUsersByPostCategory("test")).thenReturn(List.of(user));

        List<User> result = userService.findUsersByPostCategory("test");

        assertThat(result).containsExactly(user);
        verify(cacheService).put("users:category:test", List.of(user));
    }

    // Тесты для updateUser
    @Test
    void updateUser_ShouldUpdateUserWhenNoConflicts() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updateRequest);

        assertThat(result.getUsername()).isEqualTo("updateduser");
        verify(cacheService).invalidateByPrefix("users:");
    }

    @Test
    void updateUser_ShouldUpdateWhenEmailNotChanged() {
        updateRequest.setEmail("test@example.com"); // Тот же email
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updateRequest);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateUser_ShouldUpdateWhenUsernameNotChanged() {
        updateRequest.setUsername("testuser"); // Тот же username
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updateRequest);

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void updateUser_ShouldThrowWhenEmailExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void updateUser_ShouldThrowWhenUsernameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("updateduser")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void updateUser_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    // Тесты для deleteUser
    @Test
    void deleteUser_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
        verify(cacheService).invalidateByPrefix("users:");
    }

    @Test
    void deleteUser_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }
}