package example.banking_system.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Component
public class UserDao { ;
    @Autowired
    private UserRepository userRepository;

    public void addUser(User user) {
        userRepository.saveAndFlush(user);
    }

    public User findUserByLogin(String login) {
       return userRepository.findUserByLogin(login);
    }

    public User findUserByLoginAndPassword(String login, String password) {
        return userRepository.findUserByLoginAndPassword(login, password);
    }

}
