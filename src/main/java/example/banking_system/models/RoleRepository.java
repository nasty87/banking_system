package example.banking_system.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("Select r from Role r WHERE r.name = :name")
    public Role getRoleByName(@Param("name") String name);

}
