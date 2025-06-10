package com.josegomez.spring_mongo_api.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * The GlobalExceptionHandler class in this Java code handles various exceptions and generates
 * appropriate error responses for different scenarios in a Spring application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        Throwable root = ex;
        while (root.getCause() != null && root != root.getCause()) {
            root = root.getCause();
        }

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                status.getReasonPhrase(),
                request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handle404(NoHandlerFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse error = new ErrorResponse(
                status.value(),
                "Path not found",
                LocalDateTime.now(),
                status.getReasonPhrase(),
                request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
            WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                status.getReasonPhrase(),
                request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        String rawMessage = ex.getMessage();
        String message = "Duplicate key error";

        if (rawMessage != null && rawMessage.contains("dup key")) {
            int start = rawMessage.indexOf("dup key:");
            if (start != -1) {
                String dupPart = rawMessage.substring(start).replace("dup key:", "").trim();
                message += " on " + dupPart;
            }
        }

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                status.getReasonPhrase(),
                request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex,
            WebRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        HttpStatus statusHttp = HttpStatus.resolve(status.value());
        String errorPhrase = (statusHttp != null) ? statusHttp.getReasonPhrase() : status.toString();

        ErrorResponse error = new ErrorResponse(
                status.value(),
                ex.getReason(),
                LocalDateTime.now(),
                errorPhrase,
                request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(error, status);
    }
}
