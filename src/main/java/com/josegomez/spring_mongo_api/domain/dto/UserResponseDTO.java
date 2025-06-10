package com.josegomez.spring_mongo_api.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastNamePaternal;
    private String lastNameMaternal;
    private List<RoleResponseDTO> roles;
}