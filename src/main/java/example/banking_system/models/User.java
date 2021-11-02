package example.banking_system.models;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface User extends UserDetails {
    public String getName();
    public void setName(String name);

    public String getLogin();
    public void setLogin(String name);

    public void setPassword(String password);

    public List<? extends Account> getAccounts();

    public String getRoleName();

}
