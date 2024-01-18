package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.members.InOutGuildMembers;

@Repository
public interface InOutGuildMembersRepository extends JpaRepository<InOutGuildMembers, Long> {
}
