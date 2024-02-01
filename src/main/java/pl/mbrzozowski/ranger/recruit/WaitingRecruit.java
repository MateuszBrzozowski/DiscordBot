package pl.mbrzozowski.ranger.recruit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "waiting_recruit")
public class WaitingRecruit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 50, nullable = false)
    private String userId;

    public WaitingRecruit(String userId) {
        this.userId = userId;
    }
}
