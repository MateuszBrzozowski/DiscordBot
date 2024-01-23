package pl.mbrzozowski.ranger.giveaway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "giveaway_user")
public class GiveawayUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String userName;
    private LocalDateTime timestamp;
    @ManyToOne
    private Giveaway giveaway;
    @ManyToOne
    private Prize prize;
}
