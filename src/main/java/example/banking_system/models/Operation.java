package example.banking_system.models;

import java.math.BigDecimal;
import java.util.Date;

public interface Operation {
    public String getFromAccountNumber();

    public String getToAccountNumber();

    public Date getDateTime();
    void setDateTime(Date dateTime);

    public BigDecimal getSum();
    public void setSum(BigDecimal sum);

}
