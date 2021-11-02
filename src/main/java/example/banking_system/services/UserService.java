package example.banking_system.services;

import example.banking_system.models.Role;
import example.banking_system.models.RoleDao;
import example.banking_system.models.UserEntity;
import example.banking_system.models.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public boolean saveUser(UserEntity user, String roleName) {
        UserEntity userFromDB = userDao.findUserByLogin(user.getUsername());

        if (userFromDB != null) {
            return false;
        }
        Role role = roleDao.getRoleByName(roleName);
        if (role != null) {
            user.setRole(role);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.addUser(user);
        return true;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserEntity user = userDao.findUserByLogin(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Transactional
    public UserEntity findByLoginAndPassword(String login, String password) throws UsernameNotFoundException {
        UserEntity user = userDao.findUserByLoginAndPassword(login, password);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
}
