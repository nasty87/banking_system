package example.banking_system.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OperationControllerAdvice {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleException(BusinessException businessException) {
        return new ResponseEntity<>(businessException.getMessage(), businessException.getHttpStatus());
    }
}
