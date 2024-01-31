package pl.mbrzozowski.ranger.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    private String name = "";
    private LocalDateTime dateTime;
    private String description = "";
    private EventFor eventFor = EventFor.CLAN_MEMBER;
    private String authorName;
    private String authorId;
}
