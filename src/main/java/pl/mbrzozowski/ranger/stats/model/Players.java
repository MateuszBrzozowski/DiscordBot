package pl.mbrzozowski.ranger.stats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "dblog_players")
public class Players {
    @Id
    @Column(name = "steamID")
    private String steamID;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "eosID")
    private String eosID;
    @Column(name = "lastIP")
    private String lastIP;
}
