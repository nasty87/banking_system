package example.banking_system.models;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationRepository extends JpaRepository<OperationEntity, Long> {
    @Query("SELECT o FROM OperationEntity o WHERE o.fromAccount.id = :accountId OR o.toAccount.id = :accountId")
    public List<OperationEntity> getOperationHistory(@Param("accountId") Long accountId, Pageable pageable);
}

