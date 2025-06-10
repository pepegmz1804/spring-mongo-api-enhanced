package com.josegomez.spring_mongo_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDTO {

    @NotBlank(message = "Role key is required")
    @Size(max = 50, message = "Role key must be at most 50 characters")
    @Pattern(regexp = "^[a-z_]+$", message = "Role key must be lowercase and underscore-separated")
    private String key;

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be at most 50 characters")
    private String name;

}
