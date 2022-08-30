package ranger.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Data
@AllArgsConstructor
@Builder
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 30, nullable = false)
    private String userId;
    @Column(length = 30, nullable = false)
    private String userName;
    private boolean mainList;
    @ManyToOne
    private Event event;
}
