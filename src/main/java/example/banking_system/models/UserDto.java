package example.banking_system.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class UserDto implements User {
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "Login cannot be null")
    private String login;
    @NotNull(message = "Password cannot be null")
    private String password;
    List<AccountDto> accounts = new ArrayList<>();
    @NotNull(message = "Role name cannot be null")
    String roleName;

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
       return null;
    }

    @Override public String getUsername() {
        return login;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return true;
    }

    @Override public String getPassword() {
        return password;
    }


}
