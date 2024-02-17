package pl.mbrzozowski.ranger.games.timeout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class LastCall {
    private LocalDateTime dateTime;
    private int amount;
}
