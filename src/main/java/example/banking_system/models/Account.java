package example.banking_system.models;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface Account {
    public String getAccountNumber();
    public void setAccountNumber(String accountNumber);

    public OffsetDateTime getCreationDate();
    void setCreationDate(OffsetDateTime date);

    public BigDecimal getBalance();
    public void setBalance(BigDecimal balance);
}
