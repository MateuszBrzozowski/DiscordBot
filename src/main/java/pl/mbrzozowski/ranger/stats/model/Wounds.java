package pl.mbrzozowski.ranger.stats.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "DBLog_Wounds")
public class Wounds {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "time")
    private LocalDateTime time;
    @Column(name = "victimName")
    private String victimName;
    @Column(name = "victimTeamID")
    private Integer victimTeamID;
    @Column(name = "victimSquadID")
    private Integer victimSquadID;
    @Column(name = "attackerName")
    private String attackerName;
    @Column(name = "attackerTeamID")
    private Integer attackerTeamID;
    @Column(name = "attackerSquadID")
    private Integer attackerSquadID;
    @Column(name = "damage")
    private Float damage;
    @Column(name = "weapon")
    private String weapon;
    @Column(name = "teamkill")
    private Boolean teamkill;
    @Column(name = "server")
    private Integer server;
    @Column(name = "attacker")
    private String attacker;
    @Column(name = "victim")
    private String victim;
    @Column(name = "match")
    private Integer match;
}
