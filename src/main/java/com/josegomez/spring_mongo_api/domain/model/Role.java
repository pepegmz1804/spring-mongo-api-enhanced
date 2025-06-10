package com.josegomez.spring_mongo_api.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.josegomez.spring_mongo_api.domain.common.SequenceIdentifiable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Role class wit auto-generated id and validations
 */
@Document(collection = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role implements SequenceIdentifiable {

    @Id
    @Indexed(unique = true)
    private Long id;

    @Indexed(unique = true)
    @NotBlank(message = "Role key is required")
    @Size(max = 50, message = "Role key must be at most 50 characters")
    @Pattern(regexp = "^[a-z_]+$", message = "Role key must be lowercase and underscore-separated")
    private String key;

    @Indexed(unique = true)
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be at most 50 characters")
    @Pattern(regexp = "^[\\p{L}\\s\\-.'áéíóúÁÉÍÓÚñÑüÜ]+$", message = "Role name must not contain numbers or invalid characters")
    private String name;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

}