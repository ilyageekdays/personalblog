package com.example.personalblog.service;

import com.example.personalblog.cache.CacheService;
import com.example.personalblog.dto.CreateUserRequest;
import com.example.personalblog.dto.UpdateUserRequest;
import com.example.personalblog.model.User;
import com.example.personalblog.repository.UserRepository;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MSG = "User not found";

    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Autowired
    public UserService(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public User createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already taken"
            );
        }

        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setVisibleName(createUserRequest.getVisibleName());
        cacheService.invalidateByPrefix("users:");
        return userRepository.save(user);
    }

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG));
    }

    @Transactional
    public List<User> getAllUsers() {
        String cacheKey = "users:";
        List<User> cachedUsers = (List<User>) cacheService.get(cacheKey);
        if (cachedUsers != null) {
            return cachedUsers;
        }

        List<User> users = userRepository.findAll();  // или ваша логика
        cacheService.put(cacheKey, users);
        return users;
    }

    @Transactional
    public List<User> findUsersByPostCategory(String categoryName) {
        String cacheKey = "users:category:" + categoryName;
        List<User> cachedUsers = (List<User>) cacheService.get(cacheKey);
        if (cachedUsers != null) {
            return cachedUsers;
        }

        List<User> users = userRepository.findUsersByPostCategory(categoryName);  // или ваша логика
        cacheService.put(cacheKey, users);
        return users;
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG));

        if (!user.getEmail().equals(updateUserRequest.getEmail())
                && userRepository.existsByEmail(updateUserRequest.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (!user.getUsername().equals(updateUserRequest.getUsername())
                && userRepository.existsByUsername(updateUserRequest.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already taken"
            );
        }

        user.setVisibleName(updateUserRequest.getVisibleName());
        user.setUsername(updateUserRequest.getUsername());
        user.setEmail(updateUserRequest.getEmail());
        cacheService.invalidateByPrefix("users:");
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG));
        cacheService.invalidateByPrefix("users:");
        userRepository.delete(user);
    }
}