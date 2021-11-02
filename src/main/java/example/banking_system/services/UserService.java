package example.banking_system.services;

import example.banking_system.controllers.InvalidParameterException;
import example.banking_system.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public void saveUser(UserEntity user, String roleName) throws InvalidParameterException {
        UserEntity userFromDB = userDao.findUserByLogin(user.getUsername());

        if (userFromDB != null) {
            throw new InvalidParameterException();
        }
        Role role = roleDao.getRoleByName(roleName);
        if (role != null) {
            user.setRole(role);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.addUser(user);
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
    public void addUser(UserDto user) throws InvalidParameterException {
        UserEntity newUser = new UserEntity();
        newUser.setName(user.getName());
        newUser.setLogin(user.getLogin());
        newUser.setPassword(user.getPassword());

        for (AccountDto account : user.getAccounts()) {
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setAccountNumber(account.getAccountNumber());
            accountEntity.setBalance(account.getBalance());
            accountEntity.setCreationDate(account.getCreationDate());
            accountEntity.setUser(newUser);
            newUser.getAccounts().add(accountEntity);
        }

        saveUser(newUser, user.getRoleName());
    }

    @Transactional
    public UserEntity findByLoginAndPassword(String login, String password) throws UsernameNotFoundException {
        UserEntity user = userDao.findUserByLoginAndPassword(login, password);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Transactional
    public UserEntity getCurrentUser() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDao.findUserByLogin(currentUserName);
    }

    @Transactional
    public boolean userHasBankRole(UserEntity user) {
        return user.getRoleName().equals(Role.BankRoleName);
    }

}
