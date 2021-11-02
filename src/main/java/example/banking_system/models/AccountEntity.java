package example.banking_system.models;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Getter
@Setter
public class AccountEntity implements Account{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    private String accountNumber;

    private Date creationDate;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountEntity)) {
            return false;
        }
        return id == ((AccountEntity) o).getId();
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
