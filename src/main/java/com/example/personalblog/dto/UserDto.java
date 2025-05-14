package com.example.personalblog.dto;

import com.example.personalblog.model.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String visibleName;
    private String username;
    private String email;

    public static UserDto fromEntity(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setVisibleName(user.getVisibleName());
        return userDto;
    }
}
