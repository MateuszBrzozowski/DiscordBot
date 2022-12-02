package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.role.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
