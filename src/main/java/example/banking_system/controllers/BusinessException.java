package example.banking_system.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class BusinessException extends Exception {
    private String message;
    private HttpStatus httpStatus;
}
