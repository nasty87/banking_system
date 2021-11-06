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
public class OperationDto implements Operation {
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String fromAccountNumber;
    @NotNull(message = "Account number cannot be null")
    @Size(min = 20, max = 20)
    private String toAccountNumber;
    @PastOrPresent
    private OffsetDateTime dateTime;
    @Min(value = 0, message= "Sum cannot be negative")
    BigDecimal sum;


    public static final OperationDto fromOperation(Operation operation) {
        OperationDto res = new OperationDto();
        res.setFromAccountNumber(operation.getFromAccountNumber());
        res.setToAccountNumber(operation.getToAccountNumber());
        res.setDateTime(operation.getDateTime());
        res.setSum(operation.getSum());

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof OperationDto))
            return false;
        OperationDto other = (OperationDto)o;
        boolean fromAccountNumberEquals = (this.fromAccountNumber == null && other.fromAccountNumber == null)
                || (this.fromAccountNumber != null && this.fromAccountNumber.equals(other.fromAccountNumber));
        if (!fromAccountNumberEquals) {
            return false;
        }
        boolean toAccountNumberEquals = (this.toAccountNumber == null && other.toAccountNumber == null)
                || (this.toAccountNumber != null && this.toAccountNumber.equals(other.toAccountNumber));
        if (!toAccountNumberEquals) {
            return false;
        }
        boolean dateTimeEquals = (this.dateTime == null && other.dateTime == null)
                || (this.dateTime != null && this.dateTime.equals(other.dateTime));
        if (!dateTimeEquals) {
            return false;
        }
        boolean sumEquals = (this.sum == null && other.sum == null)
                || (this.sum != null && this.sum.equals(other.sum));
        if (!sumEquals) {
            return false;
        }
        return true;
    }

}
