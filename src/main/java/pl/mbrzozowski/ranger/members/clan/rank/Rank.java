package pl.mbrzozowski.ranger.members.clan.rank;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "rank_")
public class Rank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    @Column(nullable = false, unique = true)
    private String name;
    private String shortcut;
    @Column(unique = true, length = 100)
    private String discordId;

    public Optional<String> getDiscordId() {
        if (discordId == null) {
            return Optional.empty();
        }
        return Optional.of(discordId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return Objects.equals(id, rank.id) && name.equals(rank.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
