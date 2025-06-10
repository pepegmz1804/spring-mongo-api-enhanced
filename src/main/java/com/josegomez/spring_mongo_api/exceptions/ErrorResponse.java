package com.josegomez.spring_mongo_api.exceptions;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The ErrorResponse class represents an error response with status, message, timestamp, error, and
 * path information.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String error;
    private String path;

}