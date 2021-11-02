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
    public static final String AdminRoleName = "ROLE_ADMIN";
    public static final String BankRoleName = "ROLE_BANK";
    public static final String ClientRoleName = "ROLE_CLIENT";

    @Id
    private long id;

    private String name;

    public Role(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }

}
