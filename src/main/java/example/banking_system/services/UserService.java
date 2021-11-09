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
    @Autowired private UserRepository userRepository;
    @Autowired private RoleDao roleDao;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public void saveUser(UserEntity user, String roleName) {
        UserEntity userFromDB = userRepository.findUserByLogin(user.getUsername());

        if (userFromDB != null) {
            throw new InvalidParameterException();
        }
        Role role = roleDao.getRoleByName(roleName);
        if (role != null) {
            user.setRole(role);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserByLogin(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Transactional
    public void addUser(UserDto user) {
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
        UserEntity user = userRepository.findUserByLoginAndPassword(login, password);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Transactional
    public UserEntity getCurrentUser() {
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        //We dont want to make a separate DB call to get user. We would like to get it's ID, and that would be enough.
        //Hence we need to save ids, not logins in security context. But let's skip this, because setting spring security
        //correctly might be very hard
        return userRepository.findUserByLogin(currentUserName);
    }

    @Transactional
    public boolean userHasBankRole(UserEntity user) {
        return user.getRoleName().equals(Role.BANK_ROLE_NAME);
    }

}
