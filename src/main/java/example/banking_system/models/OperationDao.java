package example.banking_system.models;

import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
public class OperationDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private OperationRepository operationRepository;

    public void addOperation(@NotNull OperationEntity operation) {
        operationRepository.saveAndFlush(operation);
    }


    public List<OperationEntity> getOperationHistoryPage(@NotNull AccountEntity account, int pageNumber, int pageSize) {
        if (pageNumber >= 0)
            return operationRepository.getOperationHistory(account.getId(), PageRequest.of(pageNumber, pageSize));
        else
            return operationRepository.getOperationHistory(account.getId(), Pageable.unpaged());
    }

    public void deleteAll() {
        operationRepository.deleteAll();
    }

}
