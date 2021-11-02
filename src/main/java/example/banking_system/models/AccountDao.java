package example.banking_system.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountDao {
    @Autowired
    private AccountRepository accountRepository;

    public AccountEntity findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}
