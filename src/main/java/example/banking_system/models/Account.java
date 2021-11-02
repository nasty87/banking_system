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
public class Account {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private String accountNumber;

    private Date creationDate;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account )) {
            return false;
        }
        return id == ((Account) o).getId();
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
