package example.banking_system.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class AccountDto implements Account{
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String accountNumber;
    @PastOrPresent
    private Date creationDate;
    @Min(value = 0, message= "Balance cannot be negative")
    private BigDecimal balance;

}