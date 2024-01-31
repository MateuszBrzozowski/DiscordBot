package pl.mbrzozowski.ranger.giveaway;

import lombok.Data;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Data
public class GiveawayRequest {

    private LocalDateTime startTime = LocalDateTime.now().atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toLocalDateTime();
    private LocalDateTime endTime;
    private SelectMenuOptionTime timeDuration;
    private SelectMenuOptionTime exactDate;
    private SelectMenuOptionTime exactTime;
    private boolean isClanMemberExclude;
    private String rulesLink = "";
    private boolean mentionEveryone = false;
    private final TextChannel channelToPublish;

    public void setStartTime() {
        this.startTime = LocalDateTime.now().atZone(ZoneId.of(Constants.ZONE_ID_EUROPE_PARIS)).toLocalDateTime();
    }

    private void setEndTime() {
        if (timeDuration != null) {
            endTime = SelectMenuOptionToLocalDateTime.addTime(timeDuration, startTime);
        } else if (exactDate != null && exactTime != null) {
            endTime = SelectMenuOptionToLocalDateTime.getDate(exactDate);
            endTime = SelectMenuOptionToLocalDateTime.setTime(exactTime, endTime);
        }
    }

    public void setClanMemberExclude(@NotNull List<SelectOption> selectOptions, String selectedValue) {
        if (selectedValue == null) {
            isClanMemberExclude = false;
            return;
        }
        if (selectedValue.equals(selectOptions.get(0).getValue())) {
            isClanMemberExclude = true;
        } else if (selectedValue.equals(selectOptions.get(1).getValue())) {
            isClanMemberExclude = false;
        } else {
            throw new IllegalArgumentException("Selected value=" + selectedValue);
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
        giveaway.setRulesLink(rulesLink);
        return giveaway;
    }

    public boolean isEndTimeAfterNow() {
        setEndTime();
        if (endTime == null) {
            return false;
        }
        return endTime.isAfter(LocalDateTime.now().plusSeconds(10));
    }
}
