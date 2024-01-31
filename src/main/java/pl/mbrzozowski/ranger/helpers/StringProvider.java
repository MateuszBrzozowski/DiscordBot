package pl.mbrzozowski.ranger.helpers;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventFor;
import pl.mbrzozowski.ranger.event.EventRequest;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static pl.mbrzozowski.ranger.event.EventFor.*;
import static pl.mbrzozowski.ranger.response.EmbedSettings.BRAIN_WITH_GREEN;
import static pl.mbrzozowski.ranger.response.EmbedSettings.GREEN_CIRCLE;

public class StringProvider {

    @NotNull
    public static String removeDiscordMarkdowns(String source) {
        source = removeItalicsUnderscore(source);
        source = removeStar(source);
        source = removeGreaterThan(source);
        return source;
    }

    @NotNull
    public static String removeClanTag(@NotNull String source) {
        String result = source;
        if (result.matches("(.*)<rRangersPL>(.*)")) {
            result = result.replace("<rRangersPL>", "");
        } else if (result.matches("(.*)<RangersPL>(.*)")) {
            result = result.replace("<RangersPL>", "");
        }
        return result;
    }

    @NotNull
    private static String removeGreaterThan(@NotNull String source) {
        if (source.contains(">>>")) {
            return source.replaceAll(">>>", "\\\\>>>");
        } else {
            return source.replaceAll(">", "\\\\>");
        }
    }

    @NotNull
    private static String removeStar(@NotNull String source) {
        return source.replaceAll("\\*", "\\\\*");
    }

    @NotNull
    private static String removeItalicsUnderscore(@NotNull String source) {
        return source.replaceAll("_", "\\\\_");
    }

    @NotNull
    public static String getChannelName(@NotNull EventRequest eventRequest) {
        String name = validName(eventRequest.getName());
        String dateTime = validDateTime(eventRequest.getDateTime());
        return getChannelName(eventRequest.getEventFor(), name, dateTime);
    }

    @NotNull
    public static String getChannelName(@NotNull Event event) {
        String name = validName(event.getName());
        String dateTime = validDateTime(event.getDate());
        return getChannelName(event.getEventFor(), name, dateTime);
    }

    @NotNull
    private static String getChannelName(@NotNull EventFor eventFor, String name, String dateTime) {
        String channelName;
        if (eventFor.equals(TACTICAL_GROUP)) {
            channelName = BRAIN_WITH_GREEN;
        } else {
            channelName = GREEN_CIRCLE;
        }
        channelName += name + "-" + dateTime;
        if (channelName.length() >= 100) {
            channelName = channelName.substring(0, 100);
        }
        return channelName;
    }

    @NotNull
    private static String validDateTime(LocalDateTime dateTime) {
        String dateShort = getDateShort(dateTime);
        if (StringUtils.isBlank(dateShort)) {
            throw new IllegalArgumentException("Date of Event can not be blank");
        }
        return dateShort;
    }

    private static String validName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Event name can not be blank");
        }
        return name;
    }

    @NotNull
    private static String getDateShort(@NotNull LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        return dateTime.format(formatter);
    }

    @NotNull
    public static String getMessageForEventList(@NotNull EventRequest eventRequest) {
        if (eventRequest.getEventFor() == null) {
            return "";
        }
        String result = "";
        if (eventRequest.getEventFor().equals(CLAN_MEMBER_AND_RECRUIT)) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> <@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor().equals(RECRUIT)) {
            result = "<@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor().equals(CLAN_MEMBER)) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor().equals(TACTICAL_GROUP)) {
            result = "<@&" + RoleID.TACTICAL_GROUP + "> Tactical meeting!";
        } else if (eventRequest.getEventFor().equals(CLAN_COUNCIL)) {
            result = "<@&" + RoleID.CLAN_COUNCIL + ">!";
        }
        return result;
    }

    @NotNull
    public static String removeAnyPrefixCircle(@NotNull String channelName) {
        if (channelName.contains(EmbedSettings.BRAIN_WITH_GREEN)) {
            channelName = channelName.replaceAll(EmbedSettings.BRAIN_WITH_GREEN, "");
        }
        if (channelName.contains(EmbedSettings.BRAIN_WITH_YELLOW)) {
            channelName = channelName.replaceAll(EmbedSettings.BRAIN_WITH_YELLOW, "");
        }
        if (channelName.contains(EmbedSettings.YELLOW_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.YELLOW_CIRCLE, "");
        }
        if (channelName.contains(EmbedSettings.RED_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.RED_CIRCLE, "");
        }
        if (channelName.contains(EmbedSettings.GREEN_CIRCLE)) {
            channelName = channelName.replaceAll(EmbedSettings.GREEN_CIRCLE, "");
        }
        return channelName;
    }

    @NotNull
    public static String addYellowCircleBeforeText(String text, EventFor eventFor) {
        if (text == null) {
            text = "";
        }
        if (eventFor == EventFor.TACTICAL_GROUP) {
            text = EmbedSettings.BRAIN_WITH_YELLOW + text;
        } else {
            text = EmbedSettings.YELLOW_CIRCLE + text;
        }
        return text;
    }
}
