package ranger.recruit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Recruit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, updatable = false)
    private String userId;
    @Column(nullable = false)
    private String channelId;
    private LocalDateTime toApply;
    private LocalDateTime startRecruitment;
    private LocalDateTime endRecruitment;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RecruitmentResult recruitmentResult;
}
