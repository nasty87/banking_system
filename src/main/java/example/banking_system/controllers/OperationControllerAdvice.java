package example.banking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OperationControllerAdvice {
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<String> handleException(InvalidParameterException exception) {
        return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<String> handleException(NotAllowedException exception) {
        return new ResponseEntity<>("", HttpStatus.FORBIDDEN);
    }
}
