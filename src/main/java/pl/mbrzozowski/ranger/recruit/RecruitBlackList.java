package pl.mbrzozowski.ranger.recruit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "recruit_black_list")
public class RecruitBlackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String userId;
    private String reason;
    private LocalDateTime date;
}
