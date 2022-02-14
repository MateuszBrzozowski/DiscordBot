package event;

import embed.EmbedSettings;
import helpers.Validation;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;

public class EventsGenerator {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private JDA jda = Repository.getJda();
    private boolean here = false;
    private String userID;
    private String userName;
    private String nameEvent;
    private String date;
    private String time;
    private String description = "";
    private String perm;
    private EventGeneratorStatus stageOfGenerator = EventGeneratorStatus.SET_NAME;
    GuildMessageReceivedEvent eventMsgRec = null;
    PrivateMessageReceivedEvent eventPrivateMsgRec = null;


    public EventsGenerator(@NotNull GuildMessageReceivedEvent event) {
        userID = event.getMessage().getAuthor().getId();
        userName = event.getMessage().getAuthor().getName();
        this.eventMsgRec = event;
        embedStart();
    }

    public EventsGenerator(@NotNull PrivateMessageReceivedEvent event) {
        userID = event.getMessage().getAuthor().getId();
        userName = event.getMessage().getAuthor().getName();
        this.eventPrivateMsgRec = event;
        embedStart();
    }

    private void embedStart() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("WITAJ " + userName.toUpperCase() + " W GENERATORZE EVENTÓW!");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            builder.setDescription("Odpowiedz na kilka moich pytań. Nastepnie na podstawie Twoich odpowiedzi " +
                    "utworzę listę na Twój mecz/szkolenie/event.\n\n" +
                    "Przerwanie generowania - Wpisz tutaj **!cancel**\n" +
                    "Maksymalna liczba znaków w tytule - 256\n" +
                    "Maksymalna liczba znaków w opisie znajdującym się na liście - 2048\n\n" +
                    "Jeżeli chcesz dodać dłuższy opis wydarzenia najpierw stwórz kanał za pomocą komendy **!newChannel** \n" +
                    "-(tworzy kanał w kategorii Mecze/Szkolenia/Eventy), następnie napisz na tym kanale osobiście opis, " +
                    "zmień nazwę kanału przy pomocy komendy **!name <Twoja_Nazwa>** i stwórz listę przy pomocy komendy **!generatorHere**");
            EmbedBuilder getEventName = new EmbedBuilder();
            getEventName.setColor(Color.YELLOW);
            getEventName.addField("Podaj nazwę twojego eventu", "Maksymalna liczba znaków - 256", false);
            privateChannel.sendMessage(builder.build()).queue();
            privateChannel.sendMessage(getEventName.build()).queue();
        });
    }

    public String getUserID() {
        return userID;
    }

    void saveAnswerAndSetNextStage(PrivateMessageReceivedEvent event) {
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
            case IF_SET_DESCRIPTION: {
                if (msg.equalsIgnoreCase("T")) {
                    embedGetDescription();
                    stageOfGenerator = EventGeneratorStatus.SET_DESCRIPTION;
                } else if (msg.equalsIgnoreCase("N")) {
                    embedWhoPing();
                    stageOfGenerator = EventGeneratorStatus.SET_PERMISSION;
                } else {
                    embedIsDescriptionNotCorrect();
                    embedIsDescription();
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
            case SET_PERMISSION: {
                boolean c = msg.equalsIgnoreCase("c");
                boolean ac = msg.equalsIgnoreCase("ac");
                boolean r = msg.equalsIgnoreCase("r");
                if (c || ac || r) {
                    perm = msg;
                    embedDoYouWantAnyChange(true);
                    stageOfGenerator = EventGeneratorStatus.FINISH;
                } else {
                    embedWhoPingNotCorrect();
                    embedWhoPing();
                }
                break;
            }
            case FINISH: {
                if (msg.equalsIgnoreCase("n")) {
                    //zmiana nazwy
                    embedGetName();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_NAME;
                } else if (msg.equalsIgnoreCase("d")) {
                    //zmiana daty
                    embedGetDate();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_DATE;
                } else if (msg.equalsIgnoreCase("t")) {
                    //zmiana czasu eventu
                    embedGetTime();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_TIME;
                } else if (msg.equalsIgnoreCase("o")) {
                    //zmiana opisu eventu zawartego na liście.
                    embedGetDescription();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_DESCRIPTION;
                } else if (msg.equalsIgnoreCase("p")) {
                    embedWhoPing();
                    stageOfGenerator = EventGeneratorStatus.CHANGE_PERMISSION;
                } else if (msg.equalsIgnoreCase("show")) {
                    embedDoYouWantAnyChange(true);
                } else if (msg.equalsIgnoreCase("end")) {
                    end();
                } else {
                    embedWhoPingNotCorrect();
                    embedDoYouWantAnyChange(false);
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
            case CHANGE_PERMISSION: {
                boolean c = msg.equalsIgnoreCase("c");
                boolean ac = msg.equalsIgnoreCase("ac");
                boolean r = msg.equalsIgnoreCase("r");
                if (c || ac || r) {
                    perm = msg;
                } else {
                    embedWhoPingNotCorrect();
                }
                embedDoYouWantAnyChange(false);
                stageOfGenerator = EventGeneratorStatus.FINISH;
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
        if (eventMsgRec != null)
            e.createNewEventFromSpecificData(cmdTable, userID, eventMsgRec.getChannel());
        else e.createNewEventFromSpecificData(cmdTable, userID, null);
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

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Generwoanie listy zakończone.");
            builder.addField("Czy chcesz wprowadzić jakieś zmiany?", "N - nazwa eventu\n" +
                    "D - data eventu\n" +
                    "T - czas eventu\n" +
                    "O - opis eventu zawarty na liście\n" +
                    "P - Do kogo kierowana jest lista\n\n" +
                    "SHOW - zobacz jak bedzie wygladac lista\n" +
                    "END - kończy generowanie listy.\n" +
                    "!CANCEL - anuluje generowanie listy", false);
            privateChannel.sendMessage(builder.build()).queue();
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
        privateChannel.sendMessage(builder.build()).queue();
    }

    private String createCommand() {
        String command = "";
        if (here) command = "!zapisyhere ";
        else command = "!zapisy ";
        return command + "-name " + nameEvent + " -date " + date + " -time " + time + " -o " + description + " -" + perm;
    }

    private void embedFinish() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("GENEROWANIE LISTY ZAKOŃCZONE.");
            builder.addField("", "Sprawdź kanały na discordzie. Twój kanał i lista powinny być teraz widoczne.", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedError() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("BŁĄD");
            builder.addField("", "Generowanie Listy przerwane.", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedWhoPingNotCorrect() {

        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Twoja odpowiedź jest niepoprawna", "", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedDescriptionLong() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("UWAGA - Długi opis!", "Twój opis jest za długi żebym mógł go umieścić " +
                    "bezpośrednio na liście. Maksymalna liczba znaków - 2048", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedWhoPing() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Do kogo kierowane są zapisy.", "c - Clan Member\nac - Clan Member + Rekrut\nr - Rekrut", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedGetDescription() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj opis, który umieszczę na liście.", "Maksymalna liczba znaków - 2048", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedIsDescriptionNotCorrect() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowa odpowiedź", "", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedIsDescription() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Czy chcesz dodać opis na listę twojego eventu?", "T - TAK\nN - NIE", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedTimeNotCorrect() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowy format czasu eventu lub data i czas jest z przeszłości.", "", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedGetTime() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj czas rozpoczęcia twojego eventu", "Format: hh:mm", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedDateNotCorrect() {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowy format daty eventu lub podałeś czas z przeszłości.", "", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedGetDate() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj datę twojego eventu", "Format: dd.MM.yyyy", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedGetNameCorrect() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.addField("Nieprawidłowa nazwa eventu", "", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    private void embedGetName() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.addField("Podaj nazwę twojego eventu", "Maksymalna liczba znaków - 256", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    public void setHere(boolean here) {
        this.here = here;
    }

}
