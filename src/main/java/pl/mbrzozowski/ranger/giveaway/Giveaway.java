package pl.mbrzozowski.ranger.giveaway;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "giveaway")
public class Giveaway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;
    private String channelId;
    private String messageId;
    private boolean isClanMemberExclude;
    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "giveaway", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiveawayUser> giveawayUsers;
    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "giveaway", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prize> prizes;
}
