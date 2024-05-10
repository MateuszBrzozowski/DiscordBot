package pl.mbrzozowski.ranger.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String userName;
    private boolean mainList;
    @ManyToOne
    private Event event;
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", mainList=" + mainList +
                ", event=" + event.getName() +
                '}';
    }
}
