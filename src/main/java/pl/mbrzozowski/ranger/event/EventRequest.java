package pl.mbrzozowski.ranger.event;

import lombok.Data;

@Data
public class EventRequest {
    private String name;
    private String date;
    private String time;
    private String description;
    private EventFor eventFor = EventFor.CLAN_MEMBER;
    private String authorName;
    private String authorId;
}
