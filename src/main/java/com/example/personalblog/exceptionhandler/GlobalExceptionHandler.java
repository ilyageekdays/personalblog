package com.example.personalblog.exceptionhandler;

import com.example.personalblog.dto.ErrorResponse;
import com.example.personalblog.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Endpoint " + ex.getRequestURL() + " does not exist"
        );

        log.warn("404 – {} {}", request.getMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );

        log.warn("404 – {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("400 – {}", msg);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), msg, msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("400 – {}", msg);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), msg, msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(
            HttpMessageNotReadableException ex) {

        String msg = (ex.getCause() instanceof JsonParseException)
                ? "Malformed JSON: Check syntax (missing commas, quotes, etc.)"
                : "Invalid JSON format";

        log.warn("400 – {}", msg);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String msg = ex.getRequiredType() == LocalDate.class
                ? "Wrong date format: " + ex.getValue() + ". Use dd.MM.yyyy"
                : "Invalid parameter type";

        log.warn("400 – {}", msg);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponses(@ApiResponse(responseCode = "400", description = "Неверный формат данных"))
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        String errorMessage = ex.getMessage();
        log.warn("400 – {}", errorMessage);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        errorMessage
                ));
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ApiResponses(@ApiResponse(responseCode = "400", description = "Неверный формат даты"))
    public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex) {
        String errorMessage = String.format("Invalid date format. Expected format: yyyy-MM-dd. Error at position: %d",
                ex.getErrorIndex());

        log.warn("400 – {}", errorMessage);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        errorMessage
                ));
    }

    @ExceptionHandler(Exception.class)
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Unexpected server error"))
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("500 – Unhandled exception", ex);

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error", ex.getClass().getName()));
    }
}
