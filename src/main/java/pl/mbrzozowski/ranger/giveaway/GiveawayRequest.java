package pl.mbrzozowski.ranger.giveaway;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
public class GiveawayRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SelectMenuOption timeDuration;
    private SelectMenuOption exactDate;
    private SelectMenuOption exactTime;
    private boolean isClanMemberExclude;

    public void build() {
        this.startTime = LocalDateTime.now().atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toLocalDateTime();
        setEndTime();
    }

    private void setEndTime() {
        if (timeDuration != null) {
            endTime = SelectMenuOptionToLocalDateTime.addTime(timeDuration, startTime);
        } else if (exactDate != null && exactTime != null) {
            endTime = SelectMenuOptionToLocalDateTime.getDate(exactDate);
            endTime = SelectMenuOptionToLocalDateTime.setTime(exactTime, endTime);
        }
    }

    public void setClanMemberExclude(@NotNull SelectMenuOption excludeClanMember) {
        switch (excludeClanMember) {
            case EXCLUDE_YES -> isClanMemberExclude = true;
            case EXCLUDE_NO -> isClanMemberExclude = false;
            default -> throw new IllegalArgumentException(excludeClanMember.name());
        }
    }

    @NotNull
    public Giveaway getGiveaway() {
        if (startTime == null) {
            throw new NullPointerException("Start time is not set");
        }
        if (endTime == null) {
            throw new NullPointerException("End time is not set");
        }
        Giveaway giveaway = new Giveaway();
        giveaway.setStartTime(startTime);
        giveaway.setEndTime(endTime);
        giveaway.setClanMemberExclude(isClanMemberExclude);
        giveaway.setActive(true);
        return giveaway;
    }
}
