package pl.mbrzozowski.ranger.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mbrzozowski.ranger.members.clan.ClanMember;

@Repository
public interface ClanMemberRepository extends JpaRepository<ClanMember, Long> {
}
