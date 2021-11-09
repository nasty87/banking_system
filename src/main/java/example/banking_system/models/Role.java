package example.banking_system.models;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role implements GrantedAuthority {
    public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    public static final String BANK_ROLE_NAME = "ROLE_BANK";
    public static final String CLIENT_ROLE_NAME = "ROLE_CLIENT";

    @Id private Long id;

    private String name;

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getAuthority() {
        return name;
    }

}
