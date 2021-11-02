package example.banking_system.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "operations")
@NoArgsConstructor
@Getter
@Setter
public class OperationEntity implements Operation{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id", referencedColumnName = "id", nullable = true)
    private AccountEntity fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id", referencedColumnName = "id", nullable = true)
    private AccountEntity toAccount;

    private Date dateTime;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal sum;

    @Override
    public String getFromAccountNumber() {
        return fromAccount.getAccountNumber();
    }

    @Override
    public String getToAccountNumber() {
        return toAccount.getAccountNumber();
    }

}
