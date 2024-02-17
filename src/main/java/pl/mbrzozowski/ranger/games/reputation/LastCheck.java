package pl.mbrzozowski.ranger.games.reputation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class LastCheck {
    private LocalDateTime dateTime;
    private String channelId;
    private String messageId;
}
