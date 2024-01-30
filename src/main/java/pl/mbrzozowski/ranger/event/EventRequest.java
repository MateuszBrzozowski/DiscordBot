package pl.mbrzozowski.ranger.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class EventRequest {
    private String name = "";
    private LocalDateTime dateTime;
    private String description = "";
    private EventFor eventFor = EventFor.CLAN_MEMBER;
    private String authorName;
    private String authorId;

    public String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy");
        return dateTime.format(formatter);
    }

    public String getDateShort() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        return dateTime.format(formatter);
    }

    public String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }
}
