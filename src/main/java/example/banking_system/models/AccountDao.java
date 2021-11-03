package example.banking_system.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountDao {
    @Autowired
    private AccountRepository accountRepository;

    public AccountEntity findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public void saveAccount (AccountEntity account) {
        accountRepository.saveAndFlush(account);
    }

    public BigDecimal getBalanceForUpdate(Long id) {
        return accountRepository.getBalanceForUpdate(id);
    }

    public void setBalance(Long id, BigDecimal balance) {
        accountRepository.setBalance(id, balance);
    }
}
