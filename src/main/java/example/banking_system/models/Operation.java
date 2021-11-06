package example.banking_system.models;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface Operation {
    public String getFromAccountNumber();

    public String getToAccountNumber();

    public OffsetDateTime getDateTime();
    void setDateTime(OffsetDateTime dateTime);

    public BigDecimal getSum();
    public void setSum(BigDecimal sum);

}
