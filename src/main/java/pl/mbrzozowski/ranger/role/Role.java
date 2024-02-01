package pl.mbrzozowski.ranger.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 30, nullable = false)
    private String discordId;
    @Column(unique = true, length = 100, nullable = false)
    private String name;

    public Role(String discordId, String name) {
        this.discordId = discordId;
        this.name = name;
    }
}
