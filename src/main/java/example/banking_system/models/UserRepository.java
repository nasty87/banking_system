package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.login = :login")
    public User findUserByLogin(@Param("login") String login);

    @Query("SELECT u FROM User u WHERE u.login = :login and u.password = :password")
    public User findUserByLoginAndPassword(@Param("login") String login, @Param("password") String password);
}
