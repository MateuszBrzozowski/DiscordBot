package pl.mbrzozowski.ranger.games.birthday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "birthday")
public class Birthday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(unique = true, nullable = false)
    private String userId;
    private String userName;
    @NotNull
    @Column(nullable = false)
    private LocalDate date;
}
