package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.helpers.StringProvider;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventsEmbed {

    @NotNull
    public static EmbedBuilder getEventEmbedBuilder(@NotNull EventRequest eventRequest) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(eventRequest.getName());
        if (StringUtils.isNotBlank(eventRequest.getDescription())) {
            builder.setDescription(eventRequest.getDescription());
        }
        builder.addField(EmbedSettings.WHEN_DATE,
                Converter.LocalDateTimeToTimestampDateTimeLongFormat(eventRequest.getDateTime()) + "\n" +
                        EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(eventRequest.getDateTime()),
                true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.NAME_LIST_RESERVE + "(0)", ">>> -", true);
        builder.setFooter("Utworzony przez " + Users.getUserNicknameFromID(eventRequest.getAuthorId()));
        return builder;
    }

    @NotNull
    public static List<Button> getActionRowForEvent(@NotNull String msgId) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary(ComponentId.EVENTS_SIGN_IN + msgId, "Zapisz"));
        buttons.add(Button.secondary(ComponentId.EVENTS_SIGN_IN_RESERVE + msgId, "Rezerwa"));
        buttons.add(Button.danger(ComponentId.EVENTS_SIGN_OUT + msgId, "Wypisz"));
        return buttons;
    }

    @NotNull
    public static MessageEmbed getMessageEmbedWithUpdatedLists(Event event, @NotNull Message message) {
        List<MessageEmbed> embeds = message.getEmbeds();
        MessageEmbed mOld = embeds.get(0);
        List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
        List<MessageEmbed.Field> fieldsNew = getFieldsWithUpdatedLists(event, fieldsOld);
        return new MessageEmbed(mOld.getUrl()
                , mOld.getTitle()
                , mOld.getDescription()
                , mOld.getType()
                , mOld.getTimestamp()
                , mOld.getColorRaw()
                , mOld.getThumbnail()
                , mOld.getSiteProvider()
                , mOld.getAuthor()
                , mOld.getVideoInfo()
                , mOld.getFooter()
                , mOld.getImage()
                , fieldsNew);
    }

    @NotNull
    private static List<MessageEmbed.Field> getFieldsWithUpdatedLists(@NotNull Event event,
                                                                      @NotNull List<MessageEmbed.Field> fieldsOld) {
        List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
        String mainList = getStringOfMainList(event);
        String reserveList = getStringOfReserveList(event);
        for (int i = 0; i < fieldsOld.size(); i++) {
            if (i == 2) {
                MessageEmbed.Field fieldNew = new MessageEmbed.Field(
                        EmbedSettings.NAME_LIST + "(" + getMainListSize(event) + ")",
                        ">>> " + mainList,
                        true);
                fieldsNew.add(fieldNew);
            } else if (i == 4) {
                MessageEmbed.Field fieldNew = new MessageEmbed.Field(
                        EmbedSettings.NAME_LIST_RESERVE + "(" + getReserveListSize(event) + ")",
                        ">>> " + reserveList,
                        true);
                fieldsNew.add(fieldNew);
            } else {
                fieldsNew.add(fieldsOld.get(i));
            }
        }
        return fieldsNew;
    }

    @NotNull
    public static MessageEmbed getUpdatedEmbed(@NotNull Event event,
                                               @NotNull Message message,
                                               boolean isChangedDateTime,
                                               boolean isChangedName,
                                               boolean isChangedDescription,
                                               String description) {
        List<MessageEmbed> embeds = message.getEmbeds();
        MessageEmbed mOld = embeds.get(0);
        List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
        List<MessageEmbed.Field> fieldsNew = getFieldsWithUpdatedDateTime(event, fieldsOld, isChangedDateTime);
        String newTitle = getTitle(event, isChangedName, mOld.getTitle());
        String newDescription = getDescription(isChangedDescription, mOld.getDescription(), description);
        return new MessageEmbed(mOld.getUrl()
                , newTitle
                , newDescription
                , mOld.getType()
                , mOld.getTimestamp()
                , mOld.getColorRaw()
                , mOld.getThumbnail()
                , mOld.getSiteProvider()
                , mOld.getAuthor()
                , mOld.getVideoInfo()
                , mOld.getFooter()
                , mOld.getImage()
                , fieldsNew);
    }

    @NotNull
    private static List<MessageEmbed.Field> getFieldsWithUpdatedDateTime(@NotNull Event event,
                                                                         @NotNull List<MessageEmbed.Field> fieldsOld,
                                                                         boolean isChanged) {
        List<MessageEmbed.Field> fieldsNew = new ArrayList<>(fieldsOld.stream().toList());
        if (isChanged) {
            fieldsNew.clear();
            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 0) {
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(EmbedSettings.WHEN_DATE,
                            Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                                    EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                            true);
                    fieldsNew.add(fieldNew);
                } else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }
        }
        return fieldsNew;
    }

    private static String getTitle(@NotNull Event event, boolean isChanged, String title) {
        if (isChanged) {
            return event.getName();
        }
        return title;
    }

    private static String getDescription(boolean isChanged, String oldDescription, String newDescription) {
        if (isChanged) {
            return newDescription;
        }
        return oldDescription;
    }

    private static int getMainListSize(@NotNull Event event) {
        return event.getPlayers().stream().filter(Player::isMainList).toList().size();
    }

    private static int getReserveListSize(@NotNull Event event) {
        return event.getPlayers().stream().filter(player -> !player.isMainList()).toList().size();
    }

    private static @NotNull String getStringOfMainList(@NotNull Event event) {
        List<Player> players = new ArrayList<>(event.getPlayers().stream().filter(Player::isMainList).toList());
        if (players.size() > 0) {
            players.sort(Comparator.comparing(Player::getTimestamp));
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                String nickname = prepareNicknameToEventList(player.getUserName());
                result.append(nickname).append(" \n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    private static @NotNull String getStringOfReserveList(@NotNull Event event) {
        List<Player> players = new ArrayList<>(event.getPlayers().stream().filter(player -> !player.isMainList()).toList());
        if (players.size() > 0) {
            players.sort(Comparator.comparing(Player::getTimestamp));
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                String nickname = prepareNicknameToEventList(player.getUserName());
                result.append(nickname).append("\n");
            }
            return result.toString();
        } else {
            return "-";
        }
    }

    private static @NotNull String prepareNicknameToEventList(@NotNull String source) {
        source = StringProvider.removeClanTag(source);
        source = StringProvider.removeDiscordMarkdowns(source);
        return source;
    }
}
