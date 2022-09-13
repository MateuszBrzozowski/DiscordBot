package pl.mbrzozowski.ranger.stats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "discrduser")
public class DiscordUser {
    @Id
    @NotNull
    @Column(name = "userID", nullable = false)
    private String userID;
    @NotNull
    @Column(name = "steamID", nullable = false)
    private String steamID;
}
