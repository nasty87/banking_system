package example.banking_system.models;

import com.sun.istack.NotNull;
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
public class Operation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id", referencedColumnName = "id", nullable = true)
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id", referencedColumnName = "id", nullable = true)
    private Account toAccount;

    @NotNull
    private Date dateTime;

    @NotNull
    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal sum;


}
