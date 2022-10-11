package pl.mbrzozowski.ranger.recruit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    private String channelId;
    @Column(nullable = false)
    private LocalDateTime toApply;
    @Nullable
    private LocalDateTime startRecruitment;
    @Nullable
    private LocalDateTime endRecruitment;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Nullable
    private RecruitmentResult recruitmentResult;
}
