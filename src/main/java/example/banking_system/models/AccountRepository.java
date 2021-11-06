package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber")
    public AccountEntity findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Modifying
    @Query(value = "UPDATE accounts AS a SET balance = i.balance FROM (VALUES (:id1, :balance1), (:id2, :balance2)) as i (id, balance) WHERE i.id=a.id", nativeQuery = true)
    public void updateBalancesForTwoAccount(@Param("id1") Long firstId, @Param("balance1") BigDecimal firstBalance,
                                            @Param("id2") Long secondId, @Param("balance2") BigDecimal secondBalance);
}
