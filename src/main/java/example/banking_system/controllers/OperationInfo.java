package example.banking_system.controllers;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OperationInfo {
    private Date date;
    private String fromAccount;
    private String toAccount;
    private BigDecimal sum;
}
