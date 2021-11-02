package example.banking_system.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDao { ;
    @Autowired
    private UserRepository userRepository;

    public void addUser(UserEntity user) {
        userRepository.saveAndFlush(user);
    }

    public UserEntity findUserByLogin(String login) {
       return userRepository.findUserByLogin(login);
    }

    public UserEntity findUserByLoginAndPassword(String login, String password) {
        return userRepository.findUserByLoginAndPassword(login, password);
    }

}
