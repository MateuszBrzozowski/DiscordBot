package pl.mbrzozowski.ranger.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discord_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String discordId;
    private String name;
    private String description;

    public Role(String discordId, String name) {
        this.discordId = discordId;
        this.name = name;
    }

    public Role(String discordId, String name, String description) {
        this.discordId = discordId;
        this.name = name;
        this.description = description;
    }
}
