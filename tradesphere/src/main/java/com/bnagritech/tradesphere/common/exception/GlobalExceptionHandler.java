package com.bnagritech.tradesphere.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", ex.getMessage()));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                "message",ex.getMessage()));
    }

    @ExceptionHandler(TerritoryAlreadyExistException.class)
    public ResponseEntity<?>handleTerritoryAlreadyExists(TerritoryAlreadyExistException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }

    @ExceptionHandler(TerritoryNotFoundException.class)
    public ResponseEntity<?>handleTerritoryNotFound(TerritoryNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()
                ));
    }
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<?>handleUserNameNotFound(EmployeeNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    public ResponseEntity<?>handleEmployeeAlreadyExists(EmployeeAlreadyExistsException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(PromoterNotFoundException.class)
    public ResponseEntity<?>handlePromoterNotFound(PromoterNotFoundException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?>handleResourceAlreadyExists(ResourceAlreadyExistsException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<?>handleUserInactive(UserInactiveException ex){
        return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                .body(Map.of(
                        "message",ex.getMessage()));
    }

}
