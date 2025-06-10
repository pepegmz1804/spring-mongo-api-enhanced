package com.josegomez.spring_mongo_api.domain.common;

import java.util.List;

import org.springframework.stereotype.Component;

import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserResponseDTO;
import com.josegomez.spring_mongo_api.domain.model.User;

@Component
public class UserMapper {

    public UserResponseDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        return dto;
    }

    public List<UserResponseDTO> toListDto(List<User> users) {
        if (users == null) {
            return null;
        }
        ;
        return users.stream()
                .map(user -> {
                    return this.toDto(user);
                })
                .toList();
    }

    public User toEntity(UserRequestDTO dto) {
        if (dto == null)
            return null;
        User user = new User();
        user.setFirstName(dto.getFirstName());
        return user;
    }
}