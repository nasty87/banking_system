package example.banking_system.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// I kept this dao because of ensureRoles method
@Component
public class RoleDao {
    @Autowired
    RoleRepository roleRepository;

    public void init() {
        ensureRoles();
    }

    public void ensureRoles() {
        roleRepository.save(new Role(1L, Role.AdminRoleName));
        roleRepository.save(new Role(2L, Role.BankRoleName));
        roleRepository.save(new Role(3L, Role.ClientRoleName));
        roleRepository.flush();
    }

    protected Role getRoleByName(String name, boolean ensure) {
        Role role = roleRepository.getRoleByName(name);
        if (role == null) {
            if (ensure) {
                ensureRoles();
                return getRoleByName(name, false);
            }
            else {
                return null;
            }
        }
        return role;
    }

    public Role getRoleByName(String name) {
        return getRoleByName(name, true);
    }
}
