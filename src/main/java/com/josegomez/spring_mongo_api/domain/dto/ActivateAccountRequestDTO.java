package com.josegomez.spring_mongo_api.domain.dto;

import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivateAccountRequestDTO {

    @NotBlank(message = "Token is required")
    String token;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be at most 50 characters long")
    @Indexed(unique = true)
    String username;

    @NotBlank(message = "Password is required")
    String password;

    @NotBlank(message = "E-mail is required")
    @Size(max = 50, message = "E-mail must be at most 50 characters long")
    @Email(message = "Invalid email format")
    String email;
}
