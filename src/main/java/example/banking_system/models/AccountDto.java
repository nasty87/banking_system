package example.banking_system.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@NoArgsConstructor
@Getter
@Setter
public class AccountDto implements Account{
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String accountNumber;

    @PastOrPresent
    private OffsetDateTime creationDate;

    @Min(value = 0, message= "Balance cannot be negative")
    private BigDecimal balance;

    //TODO this like, and use it everywhere
    public static AccountDto from(Account acc){
        AccountDto res = new AccountDto();
        res.setAccountNumber(acc.getAccountNumber());
        res.setBalance(acc.getBalance());
        res.setCreationDate(acc.getCreationDate());
        return res;
    }

}