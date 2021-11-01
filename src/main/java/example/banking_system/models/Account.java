package example.banking_system.models;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @NotNull
    private String accountNumber;

    @NotNull
    private Date creationDate;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal balance;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fromAccount")
    List<Operation> operationsFrom = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "toAccount")
    List<Operation> operationsTo = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account )) return false;
        return id == ((Account) o).getId();
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
