package com.josegomez.spring_mongo_api.domain.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String username;
    private String password;
}
