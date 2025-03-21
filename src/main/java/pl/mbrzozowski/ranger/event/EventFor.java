package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum EventFor {
    CLAN_MEMBER("Clan Member", 1),
    RECRUIT("Rekruci", 1),
    CLAN_MEMBER_AND_RECRUIT("Clan Member i rekruci", 1),
    TACTICAL_GROUP("Grupa taktyczna", 1),
    COMPETITIVE_SECTION("Sekcja competitive",1),
    SQ_EVENTS("Squad Events", 2),
    CLAN_COUNCIL("Rada klanu", 1);

    private static final EventFor[] ENUMS = EventFor.values();
    private final String value;
    private final int group;

    /**
     * @param value name of target
     * @param group 1 - Clan members and recruit. If a recruits gets negative results, they are removed from events.
     *              2 - Another one
     */
    EventFor(String value, int group) {
        this.value = value;
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    public static boolean isByValue(String value) {
        for (EventFor field : ENUMS) {
            if (field.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static Collection<? extends SelectOption> getAll() {
        List<SelectOption> eventFors = new ArrayList<>();
        for (EventFor field : ENUMS) {
            eventFors.add(SelectOption.of(field.value, field.value));
        }
        return eventFors;
    }

    public String getValue() {
        return value;
    }
}
