package pl.mbrzozowski.ranger.helpers;

import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.event.EventFor;
import pl.mbrzozowski.ranger.event.EventRequest;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import static pl.mbrzozowski.ranger.event.EventFor.*;
import static pl.mbrzozowski.ranger.response.EmbedSettings.BRAIN_WITH_GREEN;
import static pl.mbrzozowski.ranger.response.EmbedSettings.GREEN_CIRCLE;

public class StringModify {

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
    public static String getStringChannelName(@NotNull EventRequest eventRequest) {
        String result;
        if (eventRequest.getEventFor() == TACTICAL_GROUP) {
            result = BRAIN_WITH_GREEN;
        } else {
            result = GREEN_CIRCLE;
        }
        result += eventRequest.getName() +
                "-" + eventRequest.getDate() + "-" + eventRequest.getTime();
        if (result.length() >= 99) {
            result = result.substring(0, 99);
        }
        return result;
    }

    @NotNull
    public static String getMessageForEventList(@NotNull EventRequest eventRequest) {
        String result = "";
        if (eventRequest.getEventFor() == CLAN_MEMBER_AND_RECRUIT) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> <@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == RECRUIT) {
            result = "<@&" + RoleID.RECRUIT_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == CLAN_MEMBER) {
            result = "<@&" + RoleID.CLAN_MEMBER_ID + "> Zapisy!";
        } else if (eventRequest.getEventFor() == TACTICAL_GROUP) {
            result = "<@&" + "RoleID.TACTICAL_GROUP" + "> Tactical meeting!";
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
    public static String addYellowCircle(String source, EventFor eventFor) {
        if (source == null) {
            source = "";
        }
        if (eventFor == EventFor.TACTICAL_GROUP) {
            source = EmbedSettings.BRAIN_WITH_YELLOW + source;
        } else {
            source = EmbedSettings.YELLOW_CIRCLE + source;
        }
        return source;
    }
}
