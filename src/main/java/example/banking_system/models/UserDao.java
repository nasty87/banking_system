package example.banking_system.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

public class UserDao {
    @PersistenceContext
    private EntityManager entityManager;

    public void addUser(User user) {
        entityManager.persist(user);
    }

    public User findUserByLogin(String login) {
        Optional<User> optional = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.login LIKE :login")
                .setParameter("login", login).getResultList().stream().findFirst();
        if (optional.isEmpty())
            return null;
        return optional.get();
    }

    public User findUserByLoginAndPassword(String login, String password) {
        Optional<User> optional = entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.login LIKE :login and u.password LIKE :password")
                .setParameter("login", login).setParameter("password", password)
                .getResultList().stream().findFirst();
        if (optional.isEmpty())
            return null;
        return optional.get();
    }

}
