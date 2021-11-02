package example.banking_system.controllers;

import example.banking_system.models.*;
import example.banking_system.security.AuthRequest;
import example.banking_system.security.AuthResponse;
import example.banking_system.security.JwtProvider;
import example.banking_system.services.UserService;
import org.hibernate.HibernateException;
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
import java.math.BigDecimal;
import java.util.Date;


@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

//    @Transactional
    @GetMapping(path = "/init")
    public String init() {
        try {
            UserEntity adminUser = new UserEntity();
            adminUser.setName("admin");
            adminUser.setLogin("admin");
            adminUser.setPassword("12345");

            UserEntity bankUser = new UserEntity();
            bankUser.setName("bank");
            bankUser.setLogin("bank");
            bankUser.setPassword("12345");

            UserEntity clientUser1 = new UserEntity();
            clientUser1.setName("client1");
            clientUser1.setLogin("client1");
            clientUser1.setPassword("12345");

            AccountEntity account1 = new AccountEntity();
            account1.setAccountNumber("11111111111111111111");
            account1.setBalance(new BigDecimal(100000));
            account1.setCreationDate(new Date());
            clientUser1.getAccounts().add(account1);
            account1.setUser(clientUser1);

            UserEntity clientUser2 = new UserEntity();
            clientUser2.setName("client2");
            clientUser2.setLogin("client2");
            clientUser2.setPassword("12345");

            AccountEntity account2 = new AccountEntity();
            account2.setAccountNumber("22222222222222222222");
            account2.setBalance(new BigDecimal(200000));
            account2.setCreationDate(new Date());
            clientUser2.getAccounts().add(account2);
            account2.setUser(clientUser2);

            UserEntity clientUser3 = new UserEntity();
            clientUser3.setName("client3");
            clientUser3.setLogin("client3");
            clientUser3.setPassword("12345");

            AccountEntity account3 = new AccountEntity();
            account3.setAccountNumber("33333333333333333333");
            account3.setBalance(new BigDecimal(300000));
            account3.setCreationDate(new Date());
            clientUser3.getAccounts().add(account3);
            account3.setUser(clientUser3);

            userService.saveUser(adminUser, "ROLE_ADMIN");
            userService.saveUser(bankUser, "ROLE_BANK");
            userService.saveUser(clientUser1, "ROLE_CLIENT");
            userService.saveUser(clientUser2, "ROLE_CLIENT");
            userService.saveUser(clientUser3, "ROLE_CLIENT");

            return "Init success!";
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }


    @Transactional
    @Secured(Role.AdminRoleName)
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
