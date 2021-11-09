package example.banking_system.controllers;

import example.banking_system.models.*;
import example.banking_system.security.AuthRequest;
import example.banking_system.security.AuthResponse;
import example.banking_system.security.JwtProvider;
import example.banking_system.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
public class UserController {
    @Autowired private UserService userService;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private AuthenticationManager authenticationManager;

    @Transactional
    @Secured(Role.ADMIN_ROLE_NAME)
    @PostMapping(path = "/users/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addUser(@NotNull @RequestBody UserDto user) throws InvalidParameterException{
        userService.addUser(user);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            UserEntity user = (UserEntity) authentication.getPrincipal();
            return ResponseEntity.ok().header(
                    HttpHeaders.AUTHORIZATION, jwtProvider.generateToken(user.getLogin()))
                    .body(new AuthResponse(user.getId(), user.getName(), user.getLogin()));
        }
        catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
