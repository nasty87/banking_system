package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.login = :login")
    public UserEntity findUserByLogin(@Param("login") String login);

    @Query("SELECT u FROM UserEntity u WHERE u.login = :login and u.password = :password")
    public UserEntity findUserByLoginAndPassword(@Param("login") String login, @Param("password") String password);
}
