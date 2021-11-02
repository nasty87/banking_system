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
public class OperationDto implements Operation {
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String fromAccountNumber;
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String toAccountNumber;
    @PastOrPresent
    private Date dateTime;
    @Min(value = 0, message= "Sum cannot be negative")
    BigDecimal sum;

}
