package example.banking_system.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

public class AccountDao {
    @PersistenceContext
    private EntityManager entityManager;

    public Account findByAccountNumber(String accountNumber) {
        Optional<Account> optional = entityManager.createQuery(
                        "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
                .setParameter("accountNumber", accountNumber).getResultList().stream().findFirst();
        if (optional.isEmpty())
            return null;
        return optional.get();
    }
}
