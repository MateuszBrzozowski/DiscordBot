package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum EventFor {
    CLAN_MEMBER("Clan Member"),
    RECRUIT("Rekruci"),
    CLAN_MEMBER_AND_RECRUIT("Clan Member i rekruci"),
    TACTICAL_GROUP("Grupa taktyczna"),
    SQ_EVENTS("Squad Events"),
    CLAN_COUNCIL("Rada klanu");

    private static final EventFor[] ENUMS = EventFor.values();
    private final String value;

    EventFor(String value) {
        this.value = value;
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
