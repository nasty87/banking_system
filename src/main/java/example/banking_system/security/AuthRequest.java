package example.banking_system.security;

import lombok.Data;

@Data
public class AuthRequest {
    private String login;
    private String password;
}
