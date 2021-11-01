package example.banking_system.models;

import com.sun.istack.NotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class OperationDao {
    @PersistenceContext
    private EntityManager entityManager;

    public void addOperation(@NotNull Operation operation) {
        if (operation.getFromAccount() != null) {
            operation.getFromAccount().setBalance(
                    operation.getFromAccount().getBalance().subtract(operation.getSum()));
            entityManager.merge(operation.getFromAccount());
        }
        if (operation.getToAccount() != null) {
            operation.getToAccount().setBalance(
                    operation.getToAccount().getBalance().add(operation.getSum()));
            entityManager.merge(operation.getToAccount());
        }
        entityManager.merge(operation);
    }

    public List<Operation> getOperationHistoryFull(@NotNull Account account) {
        return entityManager.createQuery("SELECT o FROM Operation o WHERE o.fromAccount.id = :accountId " +
                "OR o.toAccount.id = :accountId").setParameter("accountId", account.getId()).getResultList();
    }

    public List<Operation> getOperationHistoryPage(@NotNull Account account, int offset, int pageSize) {
        if (offset < 0 || pageSize <= 0)
            return getOperationHistoryFull(account);
        Query query = entityManager.createQuery("SELECT o FROM Operation o WHERE o.fromAccount.id = :accountId " +
                "OR o.toAccount.id = :accountId").setParameter("accountId", account.getId());
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

}
