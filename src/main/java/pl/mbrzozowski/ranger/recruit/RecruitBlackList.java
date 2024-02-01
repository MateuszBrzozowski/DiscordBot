package pl.mbrzozowski.ranger.recruit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mbrzozowski.ranger.helpers.Constants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public RecruitBlackList(String name, String userId, String reason) {
        this.name = name;
        this.userId = userId;
        this.reason = reason;
        this.date = LocalDateTime.now(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS));
    }
}
