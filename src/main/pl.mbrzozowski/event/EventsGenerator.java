package event;

import embed.EmbedInfo;
import embed.EmbedSettings;
import helpers.ComponentId;
import helpers.Validation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventsGenerator {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private JDA jda = Repository.getJda();
    private boolean isSpecificChannel = false;
    private String userID;
    private String userName;
    private String nameEvent;
    private String date;
    private String time;
    private String description = "";
    private String perm;
    private EventGeneratorStatus stageOfGenerator = EventGeneratorStatus.SET_NAME;
    MessageReceivedEvent messageReceived = null;


    public EventsGenerator(MessageReceivedEvent event) {
        userID = event.getMessage().getAuthor().getId();
        userName = event.getMessage().getAuthor().getName();
        this.messageReceived = event;
        embedStart();
    }

    private void embedStart() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("WITAJ " + userName.toUpperCase() + " W GENERATORZE EVENT??W!");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setDescription("Odpowiedz na kilka moich pyta??. Nastepnie na podstawie Twoich odpowiedzi " +
                    "utworz?? list?? na Tw??j mecz/szkolenie/event.\n\n" +
                    "Przerwanie generowania - Wpisz tutaj **!cancel**\n");
            EmbedBuilder getEventName = new EmbedBuilder();
            getEventName.setColor(Color.YELLOW);
            getEventName.addField("Podaj nazw?? twojego eventu", "Maksymalna liczba znak??w - 256", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
            privateChannel.sendMessageEmbeds(getEventName.build()).queue();
        });
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(ButtonInteractionEvent event) {
        switch (stageOfGenerator) {
            case IF_SET_DESCRIPTION: {
                if (event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_DESC_YES)) {
                    embedGetDescription();
                    stageOfGenerator = EventGeneratorStatus.SET_DESCRIPTION;
                } else {
                    embedWhoPing();
                    stageOfGenerator = EventGeneratorStatus.SET_PERMISSION;
                }
                disableButtons(event);
                break;
            }
            case SET_PERMISSION: {
                boolean ac = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_BOTH);
                boolean r = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_RECRUIT);
                perm = getPermString(ac, r);
                embedDoYouWantAnyChange(true);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                disableButtons(event);
                break;
            }
            case CHANGE_PERMISSION: {
                boolean ac = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_BOTH);
                boolean r = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_RECRUIT);
                perm = getPermString(ac, r);
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                disableButtons(event);
            }
            case FINISH: {
                if (event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_SHOW)) {
                    embedDoYouWantAnyChange(true);
                } else if (event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_END)) {
                    end();
                } else if (event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_CANCEL)) {
                    EmbedInfo.cancelEventGenerator(event.getUser().getId());
                    removeThisGenerator();
                }
                disableButtonsAndSelectMenu(event.getMessage());
                break;
            }
        }
    }

    public void saveAnswerAndSetNextStage(SelectMenuInteractionEvent event) {
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        String userChoose = selectedOptions.get(0).getValue();
        switch (stageOfGenerator) {
            case FINISH: {
                if (userChoose.equalsIgnoreCase(ComponentId.GENERATOR_EVENT_NAME)) {
                    embedGetName();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_NAME;
                } else if (userChoose.equalsIgnoreCase(ComponentId.GENERATOR_DATE)) {
                    embedGetDate();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_DATE;
                } else if (userChoose.equalsIgnoreCase(ComponentId.GENERATOR_TIME)) {
                    embedGetTime();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_TIME;
                } else if (userChoose.equalsIgnoreCase(ComponentId.GENERATOR_DESC)) {
                    embedGetDescription();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_DESCRIPTION;
                } else if (userChoose.equalsIgnoreCase(ComponentId.GENERATOR_WHO_PING)) {
                    embedWhoPing();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_PERMISSION;
                }
                disableButtonsAndSelectMenu(event.getMessage());
                break;
            }
        }
    }

    void saveAnswerAndSetNextStage(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        switch (stageOfGenerator) {
            case SET_NAME: {
                if (msg.length() < 256 && msg.length() > 0) {
                    nameEvent = msg;
                    stageOfGenerator = EventGeneratorStatus.SET_DATE;
                    embedGetDate();
                } else {
                    embedGetNameCorrect();
                    embedGetName();
                }
                break;
            }
            case SET_DATE: {
                boolean isDateFormat = Validation.isDateFormat(msg);
                boolean isDateAfterNow = Validation.eventDateTimeAfterNow(msg + " 23:59");
                if (isDateFormat && isDateAfterNow) {
                    date = msg;
                    stageOfGenerator = EventGeneratorStatus.SET_TIME;
                    embedGetTime();
                } else {
                    embedDateNotCorrect();
                    embedGetDate();
                }
                break;
            }
            case SET_TIME: {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                boolean isTimeAfterNow = Validation.eventDateTimeAfterNow(date + " " + msg);
                if (isTimeFormat && isTimeAfterNow) {
                    time = msg;
                    stageOfGenerator = EventGeneratorStatus.IF_SET_DESCRIPTION;
                    embedIsDescription();
                } else {
                    embedTimeNotCorrect();
                    embedGetTime();
                }
                break;
            }
            case SET_DESCRIPTION: {
                if (msg.length() < 2048) {
                    description = msg;
                    stageOfGenerator = EventGeneratorStatus.SET_PERMISSION;
                    embedWhoPing();
                } else {
                    embedDescriptionLong();
                }
                break;
            }
            case CHANGE_NAME: {
                if (msg.length() < 256 && msg.length() > 0) nameEvent = msg;
                else embedGetNameCorrect();
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                break;
            }
            case CHANGE_DESCRIPTION: {
                if (msg.length() < 2048 && msg.length() > 0) description = msg;
                else embedDescriptionLong();
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                break;
            }
            case CHANGE_DATE: {
                boolean isDateFormat = Validation.isDateFormat(msg);
                boolean timeAfterNow = Validation.eventDateTimeAfterNow(msg + " 23:59");
                if (isDateFormat && timeAfterNow) date = msg;
                else embedDateNotCorrect();
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                break;
            }
            case CHANGE_TIME: {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                boolean timeAfterNow = Validation.eventDateTimeAfterNow(date + " " + msg);
                if (isTimeFormat && timeAfterNow) time = msg;
                else embedTimeNotCorrect();
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                break;
            }
            case IF_SET_DESCRIPTION:
            case SET_PERMISSION:
            case FINISH:
            case CHANGE_PERMISSION:{
                break;
            }
            default:
                embedError();
                removeThisGenerator();
                logger.info("Default");
        }
    }

    private void end() {
        Event e = Repository.getEvent();
        String cmd = createCommand();
        String[] cmdTable = cmd.split(" ");
        if (isSpecificChannel) {
            e.createNewEventFromSpecificData(cmdTable, userID, messageReceived.getTextChannel());
        } else {
            e.createNewEventFromSpecificData(cmdTable, userID, null);
        }
        embedFinish();
        removeThisGenerator();
    }

    private void removeThisGenerator() {
        EventsGeneratorModel model = Repository.getEventsGeneratorModel();
        int index = model.userHaveActiveGenerator(userID);
        if (index >= 0) {
            model.removeGenerator(index);
        }
    }

    private void embedDoYouWantAnyChange(boolean showList) {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            if (showList) embedListExample(privateChannel);

            SelectMenu selectMenu = SelectMenu
                    .create(ComponentId.GENERATOR_FINISH_SELECT_MENU)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("Czy chcesz wprowadzi?? jakie?? zmiany?")
                    .addOption("Nazwa eventu", ComponentId.GENERATOR_EVENT_NAME)
                    .addOption("Data eventu", ComponentId.GENERATOR_DATE)
                    .addOption("Czas eventu", ComponentId.GENERATOR_TIME)
                    .addOption("Opis eventu", ComponentId.GENERATOR_DESC)
                    .addOption("Do kogo s?? zapisy?", ComponentId.GENERATOR_WHO_PING)
                    .build();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Generwoanie listy zako??czone.");
            privateChannel.sendMessageEmbeds(builder.build())
                    .setActionRows(
                            ActionRow.of(selectMenu),
                            ActionRow.of(Button.primary(ComponentId.GENERATOR_SHOW, "Poka?? list??"),
                                    Button.success(ComponentId.GENERATOR_END, "Zako??cz"),
                                    Button.danger(ComponentId.GENERATOR_CANCEL, "Anuluj")))
                    .queue();
        });
    }

    private void embedListExample(PrivateChannel privateChannel) {
        if (perm.equalsIgnoreCase("ac")) {
            privateChannel.sendMessage("CLAN_MEMBER RECRUT Zapisy!").queue();
        } else if (perm.equalsIgnoreCase("r")) {
            privateChannel.sendMessage("RECRUT Zapisy!").queue();
        } else if (perm.equalsIgnoreCase("c")) {
            privateChannel.sendMessage("CLAN_MEMBER Zapisy!").queue();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(nameEvent);
        if (description != null) {
            builder.setDescription(description + "\n");
        }
        builder.addField(EmbedSettings.WHEN_DATE, date, true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.WHEN_TIME, time, true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.NAME_LIST_RESERVE + "(0)", ">>> -", true);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private String createCommand() {
        String command = "";
        if (isSpecificChannel) command = "!zapisyhere ";
        else command = "!zapisy ";
        return command + "-name " + nameEvent + " -date " + date + " -time " + time + " -o " + description + " -" + perm;
    }

    private void embedFinish() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("GENEROWANIE LISTY ZAKO??CZONE.");
            builder.addField("", "Sprawd?? kana??y na discordzie. Tw??j kana?? i lista powinny by?? teraz widoczne.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedError() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("B????D");
            builder.addField("", "Generowanie Listy przerwane.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedWhoPingNotCorrect() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Twoja odpowied?? jest niepoprawna", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedDescriptionLong() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("UWAGA - D??ugi opis!", "Tw??j opis jest za d??ugi ??ebym m??g?? go umie??ci?? " +
                    "bezpo??rednio na li??cie. Maksymalna liczba znak??w - 2048", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedWhoPing() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Do kogo kierowane s?? zapisy?", "", false);
            privateChannel.sendMessageEmbeds(builder.build())
                    .setActionRow(
                            Button.primary(ComponentId.GENERATOR_PING_CLAN_MEMBER, "Tylko Clan Member"),
                            Button.primary(ComponentId.GENERATOR_PING_RECRUIT, "Tylko Rekrut"),
                            Button.primary(ComponentId.GENERATOR_PING_BOTH, "Clan Member + Rekrut")
                    )
                    .queue();
        });
    }

    private void embedGetDescription() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj opis, kt??ry umieszcz?? na li??cie.", "Maksymalna liczba znak??w - 2048", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedIsDescriptionNotCorrect() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawid??owa odpowied??", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedIsDescription() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Czy chcesz doda?? opis na list?? twojego eventu?", "", false);
            privateChannel.sendMessageEmbeds(builder.build())
                    .setActionRow(
                            Button.success(ComponentId.GENERATOR_DESC_YES, "Tak"),
                            Button.danger(ComponentId.GENERATOR_DESC_NO, "Nie"))
                    .queue();
        });
    }

    private void embedTimeNotCorrect() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawid??owy format czasu eventu lub data i czas jest z przesz??o??ci.", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetTime() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj czas rozpocz??cia twojego eventu", "Format: hh:mm", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedDateNotCorrect() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawid??owy format daty eventu lub poda??e?? czas z przesz??o??ci.", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetDate() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj dat?? twojego eventu", "Format: dd.MM.yyyy", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetNameCorrect() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawid??owa nazwa eventu", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetName() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj nazw?? twojego eventu", "Maksymalna liczba znak??w - 256", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    public void setSpecificChannel(boolean specificChannel) {
        this.isSpecificChannel = specificChannel;
    }

    private void disableButtons(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        List<Button> buttons = message.getButtons();
        List<Button> buttonsNew = new ArrayList<>();
        List<MessageEmbed> embeds = message.getEmbeds();
        buttons.forEach(button -> {
            button = button.asDisabled();
            buttonsNew.add(button);
        });
        MessageEmbed messageEmbed = embeds.get(0);
        message.editMessageEmbeds(messageEmbed).setActionRow(buttonsNew).queue();
    }

    private void disableButtonsAndSelectMenu(Message message) {
        message.delete().queue();
    }

    private String getPermString(boolean ac, boolean r) {
        if (r) return "r";
        else if (ac) return "ac";
        else return "c";
    }
}
