package com.josegomez.spring_mongo_api.domain.dto;

import java.util.List;

import com.josegomez.spring_mongo_api.validation.annotation.UniqueRoles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object with fields for id, key, and name, for
 * role responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$",
            message = "First name must not contain numbers or invalid characters")
    private String firstName;

    @NotBlank(message = "Last name (paternal) is required")
    @Size(max = 50, message = "Last name (paternal) must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$",
            message = "Last name (paternal) must not contain numbers or invalid characters")
    private String lastNamePaternal;

    @NotBlank(message = "Last name (maternal) is required")
    @Size(max = 50, message = "Last name (maternal) must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$",
            message = "Last name (maternal) must not contain numbers or invalid characters")
    private String lastNameMaternal;

    @NotNull(message = "Roles list cannot be null")
    @NotEmpty(message = "User must have at least one role")
    @UniqueRoles
    private List<String> roleKeys;


}
