package pl.mbrzozowski.ranger.event;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.embed.EmbedSettings;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.Validation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventsGenerator {

    private final EventRequest eventRequest = new EventRequest();
    private EventGeneratorStatus stageOfGenerator = EventGeneratorStatus.SET_NAME;
    private final EventService eventService;
    private final EventsGeneratorService eventsGeneratorService;

    @Autowired
    public EventsGenerator(@NotNull MessageReceivedEvent event,
                           EventService eventService,
                           EventsGeneratorService eventsGeneratorService) {
        this.eventService = eventService;
        this.eventsGeneratorService = eventsGeneratorService;
        eventRequest.setAuthorId(event.getMessage().getAuthor().getId());
        eventRequest.setAuthorName(event.getMessage().getAuthor().getName());
        embedStart();
    }

    private void embedStart() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("WITAJ " + eventRequest.getAuthorName().toUpperCase() + " W GENERATORZE EVENTÓW!");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setDescription("""
                    Odpowiedz na kilka moich pytań. Nastepnie na podstawie Twoich odpowiedzi utworzę listę na Twój mecz/szkolenie/event.

                    Przerwanie generowania - Wpisz tutaj **!cancel**
                    """);
            EmbedBuilder getEventName = new EmbedBuilder();
            getEventName.setColor(Color.YELLOW);
            getEventName.addField("Podaj nazwę twojego eventu", "Maksymalna liczba znaków - 256", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
            privateChannel.sendMessageEmbeds(getEventName.build()).queue();
        });
    }

    public String getUserID() {
        return eventRequest.getAuthorId();
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
                eventRequest.setEventFor(getPerm(ac, r));
                embedDoYouWantAnyChange(true);
                stageOfGenerator = EventGeneratorStatus.FINISH;
                disableButtons(event);
                break;
            }
            case CHANGE_PERMISSION: {
                boolean ac = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_BOTH);
                boolean r = event.getComponentId().equalsIgnoreCase(ComponentId.GENERATOR_PING_RECRUIT);
                eventRequest.setEventFor(getPerm(ac, r));
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
        if (stageOfGenerator == EventGeneratorStatus.FINISH) {
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
        }
    }

    void saveAnswerAndSetNextStage(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        switch (stageOfGenerator) {
            case SET_NAME -> {
                if (msg.length() < 256 && msg.length() > 0) {
                    eventRequest.setName(msg);
                    stageOfGenerator = EventGeneratorStatus.SET_DATE;
                    embedGetDate();
                } else {
                    embedGetNameCorrect();
                    embedGetName();
                }
            }
            case SET_DATE -> {
                boolean isDateFormat = Validation.isDateFormat(msg);
                boolean isDateAfterNow = Validation.eventDateTimeAfterNow(msg + " 23:59");
                if (isDateFormat && isDateAfterNow) {
                    eventRequest.setDate(msg);
                    stageOfGenerator = EventGeneratorStatus.SET_TIME;
                    embedGetTime();
                } else {
                    embedDateNotCorrect();
                    embedGetDate();
                }
            }
            case SET_TIME -> {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                boolean isTimeAfterNow = Validation.eventDateTimeAfterNow(eventRequest.getDate() + " " + msg);
                if (isTimeFormat && isTimeAfterNow) {
                    eventRequest.setTime(msg);
                    stageOfGenerator = EventGeneratorStatus.IF_SET_DESCRIPTION;
                    embedIsDescription();
                } else {
                    embedTimeNotCorrect();
                    embedGetTime();
                }
            }
            case SET_DESCRIPTION -> {
                if (msg.length() < 2048) {
                    eventRequest.setDescription(msg);
                    stageOfGenerator = EventGeneratorStatus.SET_PERMISSION;
                    embedWhoPing();
                } else {
                    embedDescriptionLong();
                }
            }
            case CHANGE_NAME -> {
                if (msg.length() < 256 && msg.length() > 0) {
                    eventRequest.setName(msg);
                } else {
                    embedGetNameCorrect();
                }
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
            }
            case CHANGE_DESCRIPTION -> {
                if (msg.length() < 2048 && msg.length() > 0) {
                    eventRequest.setDescription(msg);
                } else {
                    embedDescriptionLong();
                }
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
            }
            case CHANGE_DATE -> {
                boolean isDateFormat = Validation.isDateFormat(msg);
                boolean timeAfterNow = Validation.eventDateTimeAfterNow(msg + " 23:59");
                if (isDateFormat && timeAfterNow) {
                    eventRequest.setDate(msg);
                } else {
                    embedDateNotCorrect();
                }
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
            }
            case CHANGE_TIME -> {
                msg = Validation.timeCorrect(msg);
                boolean isTimeFormat = Validation.isTimeFormat(msg);
                boolean timeAfterNow = Validation.eventDateTimeAfterNow(eventRequest.getDate() + " " + msg);
                if (isTimeFormat && timeAfterNow) {
                    eventRequest.setTime(msg);
                } else {
                    embedTimeNotCorrect();
                }
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
            }
            case IF_SET_DESCRIPTION, SET_PERMISSION, FINISH, CHANGE_PERMISSION -> {
            }
            default -> {
                embedError();
                removeThisGenerator();
            }
        }
    }

    private void end() {
        eventService.createNewEvent(eventRequest);
        embedFinish();
        removeThisGenerator();
    }

    private void removeThisGenerator() {
        int index = eventsGeneratorService.userHaveActiveGenerator(eventRequest.getAuthorId());
        if (index >= 0) {
            eventsGeneratorService.removeGenerator(index);
        }
    }

    private void embedDoYouWantAnyChange(boolean showList) {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            if (showList) embedListExample(privateChannel);

            SelectMenu selectMenu = SelectMenu
                    .create(ComponentId.GENERATOR_FINISH_SELECT_MENU)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("Czy chcesz wprowadzić jakieś zmiany?")
                    .addOption("Nazwa eventu", ComponentId.GENERATOR_EVENT_NAME)
                    .addOption("Data eventu", ComponentId.GENERATOR_DATE)
                    .addOption("Czas eventu", ComponentId.GENERATOR_TIME)
                    .addOption("Opis eventu", ComponentId.GENERATOR_DESC)
                    .addOption("Do kogo są zapisy?", ComponentId.GENERATOR_WHO_PING)
                    .build();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Generwoanie listy zakończone.");
            privateChannel.sendMessageEmbeds(builder.build())
                    .setActionRows(
                            ActionRow.of(selectMenu),
                            ActionRow.of(Button.primary(ComponentId.GENERATOR_SHOW, "Pokaż listę"),
                                    Button.success(ComponentId.GENERATOR_END, "Zakończ"),
                                    Button.danger(ComponentId.GENERATOR_CANCEL, "Anuluj")))
                    .queue();
        });
    }

    private void embedListExample(PrivateChannel privateChannel) {
        if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER_ADN_RECRUIT) {
            privateChannel.sendMessage("CLAN_MEMBER RECRUT Zapisy!").queue();
        } else if (eventRequest.getEventFor() == EventFor.RECRUIT) {
            privateChannel.sendMessage("RECRUT Zapisy!").queue();
        } else if (eventRequest.getEventFor() == EventFor.CLAN_MEMBER) {
            privateChannel.sendMessage("CLAN_MEMBER Zapisy!").queue();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(eventRequest.getName());
        if (eventRequest.getDescription() != null) {
            builder.setDescription(eventRequest.getDescription() + "\n");
        }
        builder.addField(EmbedSettings.WHEN_DATE, eventRequest.getDate(), true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.WHEN_TIME, eventRequest.getTime(), true);
        builder.addBlankField(false);
        builder.addField(EmbedSettings.NAME_LIST + "(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(EmbedSettings.NAME_LIST_RESERVE + "(0)", ">>> -", true);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private void embedFinish() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("GENEROWANIE LISTY ZAKOŃCZONE.");
            builder.addField("", "Sprawdź kanały na discordzie. Twój kanał i lista powinny być teraz widoczne.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedError() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("BŁĄD");
            builder.addField("", "Generowanie Listy przerwane.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedDescriptionLong() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("UWAGA - Długi opis!", "Twój opis jest za długi żebym mógł go umieścić " +
                    "bezpośrednio na liście. Maksymalna liczba znaków - 2048", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedWhoPing() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Do kogo kierowane są zapisy?", "", false);
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
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj opis, który umieszczę na liście.", "Maksymalna liczba znaków - 2048", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedIsDescription() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Czy chcesz dodać opis na listę twojego eventu?", "", false);
            privateChannel.sendMessageEmbeds(builder.build())
                    .setActionRow(
                            Button.success(ComponentId.GENERATOR_DESC_YES, "Tak"),
                            Button.danger(ComponentId.GENERATOR_DESC_NO, "Nie"))
                    .queue();
        });
    }

    private void embedTimeNotCorrect() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowy format czasu eventu lub data i czas jest z przeszłości.", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetTime() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj czas rozpoczęcia twojego eventu", "Format: hh:mm", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedDateNotCorrect() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowy format daty eventu lub podałeś czas z przeszłości.", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetDate() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj datę twojego eventu", "Format: dd.MM.yyyy", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetNameCorrect() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowa nazwa eventu", "", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private void embedGetName() {
        User user = getUserById(eventRequest.getAuthorId());
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj nazwę twojego eventu", "Maksymalna liczba znaków - 256", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    private @NotNull User getUserById(String userId) {
        User user = Repository.getJda().getUserById(userId);
        if (user != null) {
            return user;
        } else {
            throw new IllegalArgumentException("User is not exist");
        }
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

    private EventFor getPerm(boolean ac, boolean r) {
        if (r) return EventFor.RECRUIT;
        else if (ac) return EventFor.CLAN_MEMBER_ADN_RECRUIT;
        else return EventFor.CLAN_MEMBER;
    }
}
