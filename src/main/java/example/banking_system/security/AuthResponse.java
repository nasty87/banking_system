package example.banking_system.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private long id;
    private String name;
    private String login;
}
