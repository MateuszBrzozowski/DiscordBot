package ranger.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String msgId;
    private String channelId;
    private LocalDateTime date;
    @OneToMany(mappedBy = "event", orphanRemoval = true)
    private List<Player> players;
}
