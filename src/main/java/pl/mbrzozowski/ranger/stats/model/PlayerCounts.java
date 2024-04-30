package pl.mbrzozowski.ranger.stats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "DBLog_PlayerCounts")
public class PlayerCounts {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "time")
    private LocalDateTime time;
    @Column(name = "players")
    private Integer players;
    @Column(name = "publicQueue")
    private Integer publicQueue;
    @Column(name = "reserveQueue")
    private Integer reserveQueue;
    @Column(name = "server")
    private Integer server;
    @Column(name = "match")
    private Integer match;
}
