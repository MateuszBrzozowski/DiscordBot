package pl.mbrzozowski.ranger.members.clan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "clan_member")
public class ClanMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nick;
    @Column(name = "rank_")
    private String rank;
    @NotNull
    @Column(nullable = false, length = 50, unique = true)
    private String discordId;
    @NotNull
    @Column(nullable = false, length = 50, unique = true)
    private String steamId;

    public ClanMember(@NotNull ClanMember clanMember) {
        this.id = clanMember.getId();
        this.nick = clanMember.getNick();
        this.rank = clanMember.getRank();
        this.discordId = clanMember.getDiscordId();
        this.steamId = clanMember.getSteamId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClanMember that = (ClanMember) o;
        return Objects.equals(id, that.id) && discordId.equals(that.discordId) && steamId.equals(that.steamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, discordId, steamId);
    }
}
