package embed;

import helpers.RangerLogger;
import model.Event;
import model.EventsGeneratorModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;

public class EventsGenerator {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private RangerLogger rangerLogger = new RangerLogger();
    private String userID;
    private String userName;
    private String nameEvent;
    private String date;
    private String time;
    private String description = null;
    private String perm;
    private int stageOfGenerator = 0;
    GuildMessageReceivedEvent eventMsgRec;



    public EventsGenerator(@NotNull GuildMessageReceivedEvent event) {
        userID = event.getMessage().getAuthor().getId();
        userName = event.getMessage().getAuthor().getName();
        this.eventMsgRec = event;
        embedStart(event);
    }

    private void embedStart(GuildMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("WITAJ " + userName.toUpperCase() + " W GENERATORZE EVENTÓW!");
                builder.setColor(Color.YELLOW);
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                builder.setDescription("Odpowiedz na kilka moich pytań. Nastepnie na podstawie Twoich odpowiedzi " +
                        "utworzę listę na Twój mecz/szkolenie/event.\n\n" +
                        "Przerwanie generowania - Wpisz tutaj **!cancel**\n" +
                        "Maksymalna liczba znaków w tytule - 256\n" +
                        "Maksymalna liczba znaków w opisie znajdującym się na liście - 2048\n" +
                        "~~Jeżeli chcesz dodać dłuższy opis wydarzenia najpierw stwórz kanał za pomocą komendy **!newChannel** -(tworzy kanał w " +
                        "kategorii Mecze/Szkolenia/Eventy, " +
                        "następnie napisz na tym kanale osobiście opis i stwórz listę przy pomocy komendy **!generatorHere**~~ <-Funkcja jeszcze nie działa.");
                EmbedBuilder getEventName = new EmbedBuilder();
                getEventName.setColor(Color.YELLOW);
                getEventName.addField("Podaj nazwę twojego eventu","Maksymalna liczba znaków - 256",false);
                privateChannel.sendMessage(builder.build()).queue();
                privateChannel.sendMessage(getEventName.build()).queue();
                stageOfGenerator++;
            });
        });
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(PrivateMessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        switch (stageOfGenerator){
            case 1:
            {
                if (msg.length()<256 && msg.length()>0){
                    nameEvent = msg;
                    stageOfGenerator++;
                    embedGetDate(event);
                }
                else {
                    embedGetNameCorrect(event);
                    embedGetName(event);
                }
                break;
            }
            case 2:
            {
                Event e = new Event();
                boolean isDateFormat =  e.isDateFormat(msg,"dd.MM.yyyy");
                if (isDateFormat){
                    date = msg;
                    stageOfGenerator++;
                    embedGetTime(event);
                }
                else {
                    embedDateNotCorrect(event);
                    embedGetDate(event);
                }
                break;
            }
            case 3:
            {
                Event e = new Event();
                boolean isTimeFormat = e.isTimeFormat(msg);
                if (isTimeFormat){
                    time = msg;
                    stageOfGenerator++;
                    embedIsDescription(event);
                }
                else {
                    embedTimeNotCorrect(event);
                    embedGetTime(event);
                }
                break;
            }
            case 4:
            {
                if (msg.equalsIgnoreCase("T")){
                    embedGetDescription(event);
                    stageOfGenerator++;
                }
                else if (msg.equalsIgnoreCase("N")){
                    embedWhoPing(event);
                    stageOfGenerator+=2;
                }
                else {
                    embedIsDescriptionNotCorrect(event);
                    embedIsDescription(event);
                }
                break;
            }
            case 5:
            {
                if (msg.length()<2048 && msg.length()>0){
                    description = msg;
                    stageOfGenerator++;
                    embedWhoPing(event);
                }
                else {
                    embedDescriptionToLong(event);
                    embedGetDescription(event);
                }
                break;
            }
            case 6:
            {
                if (msg.length()<=2){
                    if (msg.equalsIgnoreCase("c") || msg.equalsIgnoreCase("ac") || msg.equalsIgnoreCase("r")){
                        perm = msg;
                        embedDoYouWantAnyChange(event,true);
                        stageOfGenerator++;
                    }
                    else {
                        embedWhoPingNotCorrect(event);
                        embedWhoPing(event);
                    }
                }
                else {
                    embedWhoPingNotCorrect(event);
                    embedWhoPing(event);
                }
                break;
            }
            case 7:
            {
                if (msg.equalsIgnoreCase("n")){
                    //zmiana nazwy
                    embedGetName(event);
                    stageOfGenerator=8;
                }
                else if (msg.equalsIgnoreCase("d")){
                    //zmiana daty
                    embedGetDate(event);
                    stageOfGenerator=10;
                }
                else if (msg.equalsIgnoreCase("t")){
                    //zmiana czasu eventu
                    embedGetTime(event);
                    stageOfGenerator=11;
                }
                else if (msg.equalsIgnoreCase("o")){
                    //zmiana opisu eventu zawartego na liście.
                    embedGetDescription(event);
                    stageOfGenerator=9;
                }
                else if (msg.equalsIgnoreCase("p")){
                    embedWhoPing(event);
                    stageOfGenerator=12;
                }
                else if (msg.equalsIgnoreCase("show")){
                    embedDoYouWantAnyChange(event,true);
                }
                else if (msg.equalsIgnoreCase("end")){
                    //konczymy generowanie
                    Event e = RangerBot.getMatches();
                    String cmd = createCommand();
                    String[] cmdTable = cmd.split(" ");
                    e.createNewEventFromSpecificData(cmdTable,eventMsgRec);
                    embedFinish(event);
                    EventsGeneratorModel model = RangerBot.getEventsGeneratorModel();
                    int index = model.userHaveActiveGenerator(event.getAuthor().getId());
                    if (index>=0){
                        model.removeGenerator(index);
                    }
                }
                else {
                    embedWhoPingNotCorrect(event);
                    embedDoYouWantAnyChange(event,false);
                }
                break;
            }
            case 8:
            {
                if (msg.length()<256 && msg.length()>0) nameEvent = msg;
                else embedGetNameCorrect(event);
                embedDoYouWantAnyChange(event,false);
                stageOfGenerator=7;
                break;
            }
            case 9:
            {
                if (msg.length()<2048 && msg.length()>0) description = msg;
                else embedDescriptionToLong(event);
                embedDoYouWantAnyChange(event,false);
                stageOfGenerator=7;
                break;
            }
            case 10:
            {
                Event e = new Event();
                boolean isDateFormat = e.isDateFormat(msg,"dd.MM.yyyy");
                if (isDateFormat) date = msg;
                embedDoYouWantAnyChange(event,false);
                stageOfGenerator=7;
                break;
            }
            case 11:
            {
                Event e = new Event();
                boolean isTimeFormat = e.isTimeFormat(msg);
                if (isTimeFormat) time = msg;
                embedDoYouWantAnyChange(event,false);
                stageOfGenerator=7;
                break;
            }
            case 12:
            {
                boolean b = msg.equalsIgnoreCase("c");
                boolean b1 = msg.equalsIgnoreCase("ac");
                boolean b2 = msg.equalsIgnoreCase("r");
                if (b || b1 ||b2 ) {
                    perm = msg;
                }
                embedDoYouWantAnyChange(event,false);
                stageOfGenerator=7;
                break;
            }
            default:
                logger.info("Default");
        }
    }

    private void embedDoYouWantAnyChange(PrivateMessageReceivedEvent event,boolean showList) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                if (showList) embedListExample(event,privateChannel);

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.setTitle("Generwoanie listy zakończone.");
                builder.addField("Czy chcesz wprowadzić jakieś zmiany?","N - nazwa eventu\n" +
                        "D - data eventu\n" +
                        "T - czas eventu\n" +
                        "O - opis eventu zawarty na liście\n" +
                        "P - Do kogo kierowana jest lista\n\n" +
                        "SHOW - zobacz jak bedzie wygladac lista\n" +
                        "END - kończy generowanie listy.\n" +
                        "!CANCEL - anuluje generowanie listy",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedListExample(PrivateMessageReceivedEvent event, PrivateChannel privateChannel) {
        if (perm.equalsIgnoreCase("ac")){
            privateChannel.sendMessage("CLAN_MEMBER RECRUT Zapisy!").queue();
        }
        else if(perm.equalsIgnoreCase("r")){
            privateChannel.sendMessage("RECRUT Zapisy!").queue();
        }
        else if (perm.equalsIgnoreCase("c")){
            privateChannel.sendMessage("CLAN_MEMBER Zapisy!").queue();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        builder.setTitle(nameEvent);
        if (description!=null){
            builder.setDescription(description+"\n");
        }
        builder.addField(":date: Kiedy", date, true);
        builder.addBlankField(true);
        builder.addField(":clock930: Godzina", time, true);
        builder.addBlankField(false);
        builder.addField(Event.NAME_LIST+"(0)", ">>> -", true);
        builder.addBlankField(true);
        builder.addField(Event.NAME_LIST_RESERVE+"(0)", ">>> -", true);
        privateChannel.sendMessage(builder.build()).queue();
    }

    private String createCommand() {
        return "!zapisy -name " + nameEvent + " -date " + date + " -time " + time + " -o " + description + " -" + perm;
    }

    private void embedFinish(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("GENEROWANIE LISTY ZAKOŃCZONE.");
                builder.addField("","Sprawdź kanały na discordzie. Twój kanał i lista powinny być teraz widoczne.",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedWhoPingNotCorrect(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Twoja odpowiedź jest niepoprawna","",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedDescriptionToLong(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Opis za długi!","",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedWhoPing(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Do kogo kierowane są zapisy.","c - Clan Member\nac - Clan Member + Rekrut\nr - Rekrut",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedGetDescription(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Podaj opis, który umieszczę na liście.","Maksymalna liczba znaków - 2048",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedIsDescriptionNotCorrect(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Nieprawidłowa odpowiedź","",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedIsDescription(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Czy chcesz dodać opis na listę twojego eventu?","T - TAK\nN - NIE",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedTimeNotCorrect(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Nieprawidłowy format czasu eventu","",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedGetTime(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Podaj czas rozpoczęcia twojego eventu","Format: hh:mm",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedDateNotCorrect(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Nieprawidłowy format daty eventu","",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedGetDate(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Podaj datę twojego eventu","Format: dd.MM.yyyy",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    private void embedGetNameCorrect(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.addField("Nieprawidłowa nazwa eventu","",false);
                privateChannel.sendMessage(builder.build()).queue();
                });
        });
    }

    private void embedGetName(PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.YELLOW);
                builder.addField("Podaj nazwę twojego eventu","Maksymalna liczba znaków - 256",false);
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}
