package example.banking_system.controllers;

import com.sun.istack.NotNull;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;


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
            User adminUser = new User();
            adminUser.setName("admin");
            adminUser.setLogin("admin");
            adminUser.setPassword("12345");

            User bankUser = new User();
            bankUser.setName("bank");
            bankUser.setLogin("bank");
            bankUser.setPassword("12345");

            User clientUser1 = new User();
            clientUser1.setName("client1");
            clientUser1.setLogin("client1");
            clientUser1.setPassword("12345");

            Account account1 = new Account();
            account1.setAccountNumber("111111");
            account1.setBalance(new BigDecimal(100000));
            account1.setCreationDate(new Date());
//            clientUser1.addAccount(account1);
            clientUser1.getAccounts().add(account1);
            account1.setUser(clientUser1);

            User clientUser2 = new User();
            clientUser2.setName("client2");
            clientUser2.setLogin("client2");
            clientUser2.setPassword("12345");

            Account account2 = new Account();
            account2.setAccountNumber("222222");
            account2.setBalance(new BigDecimal(200000));
            account2.setCreationDate(new Date());
//            clientUser2.addAccount(account2);
            clientUser2.getAccounts().add(account2);
            account2.setUser(clientUser2);

            User clientUser3 = new User();
            clientUser3.setName("client3");
            clientUser3.setLogin("client3");
            clientUser3.setPassword("12345");

            Account account3 = new Account();
            account3.setAccountNumber("333333");
            account3.setBalance(new BigDecimal(300000));
            account3.setCreationDate(new Date());
//            clientUser3.addAccount(account3);
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
    @PostMapping(path = "/users/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String addUser(@NotNull @RequestBody User user) throws BusinessException{
        if (user.getName().isEmpty() || user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new BusinessException("User creation error: invalid parameter(s)!", HttpStatus.BAD_REQUEST);
        }
        try {
            User newUser = new User();
            newUser.setName(user.getName());
            newUser.setLogin(user.getLogin());
            newUser.setPassword(user.getPassword());

            Account newAccount = null;
            Optional<Account> account = user.getAccounts().stream().findFirst();
            if (!account.isEmpty() && !account.get().getAccountNumber().isEmpty()) {
                newAccount = new Account();
                newAccount.setAccountNumber(account.get().getAccountNumber());
                newAccount.setBalance(account.get().getBalance());
                newAccount.setCreationDate(account.get().getCreationDate());
                newUser.getAccounts().add(newAccount);
                newAccount.setUser(newUser);
            }

            if (userService.saveUser(newUser, user.getRole().getName())) {
                return "User created!";
            }

            else
                throw new BusinessException("User creation error: such login already exists!", HttpStatus.FORBIDDEN);
        }
        catch (HibernateException he) {
            throw new BusinessException("User creation error: can't connect to data base", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok().header(
                    HttpHeaders.AUTHORIZATION, jwtProvider.generateToken(user.getLogin()))
                    .body(new AuthResponse(user.getId(), user.getName(), user.getLogin()));
        }
        catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
