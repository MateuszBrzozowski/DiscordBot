package pl.mbrzozowski.ranger.games.giveaway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "giveaway_prize")
public class Prize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int numberOfPrizes;
    @ManyToOne
    private Giveaway giveaway;
    @ToString.Exclude
    @OneToMany(mappedBy = "prize", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GiveawayUser> giveawayUsers;
}
