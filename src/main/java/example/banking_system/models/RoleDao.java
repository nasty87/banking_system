package example.banking_system.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

public class RoleDao {
    @PersistenceContext
    private EntityManager entityManager;

    public void init() {
        ensureRoles();
    }

    public void ensureRoles() {
        entityManager.merge(new Role(1, "ROLE_ADMIN"));
        entityManager.merge(new Role(2, "ROLE_BANK"));
        entityManager.merge(new Role(3, "ROLE_CLIENT"));
    }

    protected Role getRoleByName(String name, boolean ensure) {
        Optional<Role> optional = entityManager.createQuery(
                        "Select r from Role r WHERE r.name LIKE :name")
                .setParameter("name", name).getResultList().stream().findFirst();
        if (optional.isEmpty()) {
            if (ensure) {
                ensureRoles();
                return getRoleByName(name, false);
            }
            else {
                return null;
            }
        }
        return optional.get();
    }

    public Role getRoleByName(String name) {
        return getRoleByName(name, true);
    }
}
