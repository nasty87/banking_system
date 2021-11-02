package example.banking_system.models;

import java.math.BigDecimal;
import java.util.Date;

public interface Account {
    public String getAccountNumber();
    public void setAccountNumber(String accountNumber);

    public Date getCreationDate();
    void setCreationDate(Date date);

    public BigDecimal getBalance();
    public void setBalance(BigDecimal balance);




}
