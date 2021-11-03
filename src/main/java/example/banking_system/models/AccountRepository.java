package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber")
    public AccountEntity findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query(value = "SELECT balance FROM accounts WHERE id = :id FOR UPDATE", nativeQuery = true)
    public BigDecimal getBalanceForUpdate(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE accounts SET balance = :balance WHERE id = :id", nativeQuery = true)
    public void setBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);
}
