package com.josegomez.spring_mongo_api.domain.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.josegomez.spring_mongo_api.domain.common.SequenceIdentifiable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements SequenceIdentifiable {

    @Id
    @Indexed(unique = true)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$", message = "First name must not contain numbers or invalid characters")
    private String firstName;

    @NotBlank(message = "Last name (paternal) is required")
    @Size(max = 50, message = "Last name (paternal) must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$", message = "Last name (paternal) must not contain numbers or invalid characters")
    private String lastNamePaternal;

    @NotBlank(message = "Last name (maternal) is required")
    @Size(max = 50, message = "Last name (maternal) must be at most 50 characters long")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$", message = "Last name (maternal) must not contain numbers or invalid characters")
    private String lastNameMaternal;

    @NotEmpty(message = "User must have at least one role")
    private List<Long> roles;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}