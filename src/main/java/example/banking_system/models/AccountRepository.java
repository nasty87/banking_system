package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.accountNumber = :accountNumber")
    public AccountEntity findByAccountNumber(@Param("accountNumber") String accountNumber);
}
