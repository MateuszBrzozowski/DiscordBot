package pl.mbrzozowski.ranger.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.exceptions.FullListException;
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
        builder.setFooter("Utworzony przez " + Users.getUserNicknameFromID(eventRequest.getAuthorId()));
        return builder;
    }

    @NotNull
    public static MessageEmbed getEventEmbedBuilder(@NotNull Event event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(event.getName());
        builder.addField(EmbedSettings.WHEN_DATE,
                Converter.LocalDateTimeToTimestampDateTimeLongFormat(event.getDate()) + "\n" +
                        EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(event.getDate()),
                true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.setFooter("Utworzony przez ---");
        return builder.build();
    }

    @NotNull
    public static List<Button> getActionRowForEvent(@NotNull String msgId) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary(ComponentId.EVENTS_SIGN_IN + msgId, "Zapisz"));
        buttons.add(Button.danger(ComponentId.EVENTS_SIGN_OUT + msgId, "Wypisz"));
        return buttons;
    }

    @NotNull
    public static MessageEmbed getMessageEmbedWithUpdatedLists(Event event,
                                                               @NotNull Message message,
                                                               String mainList) {
        List<MessageEmbed> embeds = message.getEmbeds();
        MessageEmbed mOld;
        if (embeds.isEmpty()) {
            mOld = getEventEmbedBuilder(event);
        } else {
            mOld = embeds.get(0);
        }
        List<Field> fieldsOld = mOld.getFields();
        List<Field> fieldsNew = getFieldsWithUpdatedLists(event, fieldsOld, mainList);
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
    private static List<Field> getFieldsWithUpdatedLists(@NotNull Event event,
                                                         @NotNull List<Field> fieldsOld,
                                                         String mainList) {
        List<Field> fieldsNew = new ArrayList<>();
        for (int i = 0; i < fieldsOld.size(); i++) {
            if (i == 2) {
                Field fieldNew = new Field(
                        EmbedSettings.NAME_LIST + "(" + getMainListSize(event) + ")",
                        ">>> " + mainList,
                        true);
                fieldsNew.add(fieldNew);
            } else if (i == 0 || i == 1) {
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
        List<Field> fieldsOld = embeds.get(0).getFields();
        List<Field> fieldsNew = getFieldsWithUpdatedDateTime(event, fieldsOld, isChangedDateTime);
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
    private static List<Field> getFieldsWithUpdatedDateTime(@NotNull Event event,
                                                            @NotNull List<Field> fieldsOld,
                                                            boolean isChanged) {
        List<Field> fieldsNew = new ArrayList<>(fieldsOld.stream().toList());
        if (isChanged) {
            fieldsNew.clear();
            for (int i = 0; i < fieldsOld.size(); i++) {
                if (i == 0) {
                    Field fieldNew = new Field(EmbedSettings.WHEN_DATE,
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
        return event.getPlayers().stream().toList().size();
    }

    public static @NotNull String getStringOfMainList(@NotNull Event event) throws FullListException {
        List<Player> players = new ArrayList<>(event.getPlayers().stream().toList());
        if (players.size() > 0) {
            players.sort(Comparator.comparing(Player::getTimestamp));
            StringBuilder result = new StringBuilder();
            for (Player player : players) {
                String nickname = prepareNicknameToEventList(player.getUserName());
                result.append(nickname).append("\n");
            }
            if (result.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
                throw new FullListException("Main list of event [" + event.getName() + "] is full.");
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
