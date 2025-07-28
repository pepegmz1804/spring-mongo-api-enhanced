package com.josegomez.spring_mongo_api.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("initializationTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitializationTask {
    @Id    
    @Indexed(unique = true)
    private String key;
    private String name; // nombre único de la tarea-
    private String description;
    private boolean executed;
    private Instant executedAt;

}
