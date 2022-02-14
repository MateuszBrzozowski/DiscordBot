package embed;

import event.Event;
import event.EventChanges;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;
import recrut.Recruits;

import java.awt.*;

public class EmbedInfo {

    protected static final Logger logger = LoggerFactory.getLogger(EmbedInfo.class.getName());
    private static String footer = "RangerBot created by Brzozaaa © 2021";

    /**
     * Wysyła informację, że użytkownik nie może zmienić nazwy kanału.
     *
     * @param userID ID użytkownika który chce zmienić nazwę kanału
     */
    public static void cantChangeTitle(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Oszalałeś? Nie możesz zmienić nazwy tego kanału!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika o userID, że jest już zapisany na liście eventu.
     *
     * @param userID ID użytkownika
     */
    public static void cantSignIn(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Jesteś już na liście");
            builder.setDescription("Jesteś już na głównej liście w meczu na który próbowałeś się zapisać.");
            builder.addField("", "Jeżeli nie widzisz siebie na liście, nie możesz się zapisać bo otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika o userID, że jest już zapisany na liście rezerwowej eventu.
     *
     * @param userID ID użytkownika
     */
    public static void cantSignInReserve(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Jesteś już na liście rezerwowej");
            builder.setDescription("Jesteś już na rezerwowej liście w meczu na który próbowałeś się zapisać.");
            builder.addField("", "Jeżeli nie widzisz siebie na liście, nie możesz się zapisać bo otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika o userID, że jest nie jest zapisany na event więc nie może się wypisać.
     *
     * @param userID ID użytkownika
     */
    public static void cantSignOut(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Nie możesz wypisać się z tego meczu.");
            builder.setDescription("Nie możesz wypisać się z meczu na który się nie zapisałeś!");
            builder.addField("", "Jeżeli jednak jesteś na liście a nadal otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika o userID, że podana przez niego data lub czas jest nieprawidłowe.
     *
     * @param userID ID użytkownika
     */
    public static void wrongDateOrTime(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("Podałeś nieprawidłowe dane.");
            builder.setDescription("Format daty: dd.MM.yyyy\n" +
                    "Format czasu: hh:mm");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika że podał date i czas z przeszłości.
     *
     * @param userID ID użytkownika do którego wysyłana jest informacja
     */
    public static void dateTimeIsBeforeNow(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Podałeś nielogiczną datę i czas. Event w przeszłości?");
            builder.setDescription("Podaj prawidłową datę i czas.");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wyświetla informację, że kanał podany w parametrze został zamknięty.
     *
     * @param userID  ID użytkownika który zamyka kanał
     * @param channel Kanał który został zamknięty.
     */
    public static void closeChannel(String userID, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Kanał zamknięty");
        builder.setDescription("Kanał zamknięty przez " + Users.getUserNicknameFromID(userID) + ".");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }

    /**
     * Wyświetla informację, że kanał podany w parametrze został otwarty.
     *
     * @param userID  ID użytkownika który zamyka kanał
     * @param channel Kanał który został otwarty.
     */
    public static void openChannel(String userID, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Kanał otwarty");
        builder.setDescription("Kanał otwarty przez " + Users.getUserNicknameFromID(userID) + ".");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }

    /**
     * Wyświetla informację że kanał został usunięty i że za chwilę zniknie.
     *
     * @param channel Kanał który został usunięty.b
     */
    public static void removedChannel(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Kanał usunięty.");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }

    /**
     * Pinguje rekruta który jest przypisany do kanału rekrutacji (textChannel) i wysyła negatywną informację.
     *
     * @param userID  ID uzytkownika który wystawia wynik.
     * @param channel Kanał na którym ocena jest wystawiana.
     */
    public static void endNegative(String userID, TextChannel channel) {
        Recruits recruits = Repository.getRecruits();
        if (recruits.isRecruitChannel(channel.getId())) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle(EmbedSettings.RESULT + "NEGATYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(userID));
            channel.sendMessage("<@" + recruits.getRecruitIDFromChannelID(channel.getId()) + ">").embed(builder.build()).queue();
        }
    }

    /**
     * Pinguje rekruta który jest przypisany do kanału rekrutacji (textChannel) i wysyła pozytywną informację.
     *
     * @param userID  ID uzytkownika który wystawia wynik.
     * @param channel Kanał na którym ocena jest wystawiana.
     */
    public static void endPositive(String userID, TextChannel channel) {
        Recruits recruits = Repository.getRecruits();
        if (recruits.isRecruitChannel(channel.getId())) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle(EmbedSettings.RESULT + "POZYTYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(userID));
            channel.sendMessage("Gratulacje <@" + recruits.getRecruitIDFromChannelID(channel.getId()) + ">").embed(builder.build()).queue();
        }
    }

    /**
     * Wysyła do użytkownika o ID userID informację że ma już złożone podanie i nie może mieć więcej niż jednego.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void userHaveRecrutChannel(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ WIĘCEJ NIŻ JEDNO PODANIE!");
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setDescription("Zlożyłeś już podanie do naszego klanu i\n" +
                    "jesteś w trakcie rekrutacji.\n");
            builder.addField("Jeżeli masz pytania w związku z Twoją rekrutacją", "", false);
            builder.addField("1. Spradź kanały", "Znajdź kanał przypisany do twojej rekrutacji i napisz do nas.", false);
            builder.addField("2.Nie widze kanału.", "Jeżeli nie widzisz kanału przypisanego do twojej rekrutacji " +
                    "skontaktuj się z Drill Instrutor. Znajdziesz ich po prawej stronie na liście użytkowników.", false);
            privateChannel.sendMessage(builder.build()).queue();
            RangerLogger.info("Użytkonik [" + Users.getUserName(userID) + "] chciał złożyć podanie. Ma otwarty kanał rekrutacji.");
        });
    }

    /**
     * Wysyła do użytkownika o ID userID informację że jest już w klanie nie może złożyć podania na rekrutację.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void userIsInClanMember(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setDescription("Jesteś już w naszym klanie dzbanie!");
            privateChannel.sendMessage(builder.build()).queue();
            RangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Jest już w naszym klanie.");
        });
    }

    /**
     * Wysyła do użytkownika o ID userID informację że jest już w innym klanie nie może złożyć podania na rekrutację.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void userIsInClan(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setColor(Color.red);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setDescription("Masz przypisaną rolę innego klanu na naszym discordzie.");
            builder.addBlankField(false);
            builder.addField("- Nie należę do żadnego klanu", "Proszę znajdź użytkownika z rolą Rada klanu na naszym discordzie i " +
                    "napisz do nas.", false);
            privateChannel.sendMessage(builder.build()).queue();
            RangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Ma przypisaną rolę innego klanu.");
        });
    }

    /**
     * Wysyła informację, że użytkownik o ID miał już otwarty generator.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void userHaveActiveEventGenerator(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("MIAŁEŚ AKTYWNY GENERATOR");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację do użytkownika o ID że otwierany jest dla niego nowy kanał.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void createNewGenerator(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("OTWIERAM NOWY GENERATOR");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Jeżeli użytkownik nie jest botem usuwa wiadomość i wysyła informację że kanał służy do logowania i tam nie piszemy.
     *
     * @param event Wydarzenie napisania wiadomości na kanale tekstowym.
     */
    public static void noWriteOnLoggerChannel(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getMessage().delete().submit();
            JDA jda = Repository.getJda();
            jda.getUserById(event.getAuthor().getId()).openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setTitle("Zachowajmy porządek!");
                builder.setDescription("Panie administratorze! Zachowajmy czystość na kanale do loggowania. Proszę nie wtrącać się w moje wypociny.");
                builder.setFooter("RangerBot created by Brzozaaa © 2021");
                builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
                privateChannel.sendMessage(builder.build()).queue();
            });
        }
    }


    /**
     * Wysyła do użytkownika o userID informację że event już się wydarzył.
     *
     * @param userID ID użytkownika do którego wysyłana jest wiadomość
     */
    public static void eventIsBefore(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Event już się wydarzył. Nie możesz się zapisać.");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła do użytkownika o userID powiadomienie że nie może wypisać się 3h przed eventem
     *
     * @param userID  ID użytkownika do którego wysyna jest wiadomość
     * @param eventID ID eventu z którego próbuje wypisać się uzytkownik
     */
    public static void youCantSingOut(String userID, String eventID) {
        JDA jda = Repository.getJda();
        Event event = Repository.getEvent();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Nie możesz wypisać się z eventu tuż przed jego rozpoczęciem!");
            builder.setDescription("Jeżeli nie możesz pojawić się z ważnych przyczyn przekaż informację na kanale eventu dlaczego Cię nie będzie");
            String linkToEventChannel = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" +
                    event.getChannelID(eventID) + ")";
            builder.addField("Link do eventu", linkToEventChannel, false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła do użytkownika o userID powiadomienie że nie może wypisać się 3h przed eventem z głównej listy na rezerwową
     *
     * @param userID  ID użytkownika do którego wysyna jest wiadomość
     * @param eventID ID eventu z którego próbuje wypisać się uzytkownik
     */
    public static void youCantSignReserve(String userID, String eventID) {
        JDA jda = Repository.getJda();
        Event event = Repository.getEvent();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            builder.setTitle("Nie możesz wypisać się z głownej listy na rezerwową tuż przed rozpoczęciem eventu!");
            builder.setDescription("Jeżeli istnieje ryzyko, że się spóźnisz powiadom nas na kanale eventu");
            String linkToEventChannel = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" +
                    event.getChannelID(eventID) + ")";
            builder.addField("Link do eventu", linkToEventChannel, false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }


    /**
     * Wysyła iformację o statusie aplikacji jeżeli użytkownik do twórca aplikacji.
     *
     * @param userID ID użytkownika
     */
    public static void sendStatus(String userID) {
        if (Users.isUserDev(userID)) {
            JDA jda = Repository.getJda();
            jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
                Event events = Repository.getEvent();
                Recruits recruits = Repository.getRecruits();
                events.sendInfo(privateChannel);
                recruits.sendInfo(privateChannel);
            });
        }
    }

    /**
     * Wysyła informację że pomyślnie wyłączono przypomnienia dla eventów.
     *
     * @param userID ID użytkownika
     */
    public static void reminderOff(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Przypomnienia wyłączone.");
            builder.setDescription("Aby włączyć ponownie przypomnienia użyj komendy **!reminder On**");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła informację że pomyślnie włączono przypomnienia dla eventów.
     *
     * @param userID ID użytkownika
     */
    public static void reminderOn(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Przypomnienia włączone.");
            builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !help Reminder");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    /**
     * Wysyła do użytkownika o ID userID wiadomość o zmianach w evencie.
     *
     * @param userID     ID użytkownika
     * @param eventID    ID eventu
     * @param whatChange
     * @param dateTime
     */
    public static void sendInfoChanges(String userID, String eventID, EventChanges whatChange, String dateTime) {
        String description = "";
        if (whatChange.equals(EventChanges.CHANGES)) {
            description = "Zmieniona data lub czas wydarzenia na które się zapisałeś.";
        } else if (whatChange.equals(EventChanges.REMOVE)) {
            description = "Wydarzenie zostaje odwołane.";
        }
        Event event = Repository.getEvent();
        String link = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" + CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelID(eventID) + "/" + eventID + ")";
        JDA jda = Repository.getJda();
        String finalDescription = description + " Sprawdź szczegóły!";
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("**UWAGA:** Zmiany w wydarzeniu.");
            builder.setDescription(finalDescription);
            builder.addField("Szczegóły eventu", link + "\n:date: " + dateTime, false);
            privateChannel.sendMessage(builder.build()).queue();
            logger.info("USER: {} -  wysłałem powiadomienie", userID);
        });
    }

    /**
     * Wysyła informację że generowanie listy zostało przerwane.
     *
     * @param userID ID użytkownika do którego wysyłana jest informacja
     */
    public static void cancelEventGenerator(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("GENEROWANIE LISTY ZOSTAŁO PRZERWANE");
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    public static void cancelEventEditing(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Edytor zamknięty");
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    public static void sendHelloMessagePrivate(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle("Witamy na Discordzie klanu Rangers Polska");
            builder.setDescription("Bla bla bla bla bla\nBla bla bla bla bla\nBla bla bla bla bla\n");
            builder.setThumbnail(EmbedSettings.THUMBNAIL_FLAG_PL);
            builder.setImage(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessage(builder.build()).queue();

            EmbedBuilder builderEng = new EmbedBuilder();
            builderEng.setColor(Color.YELLOW);
            builderEng.setTitle("Witamy na Discordzie klanu Rangers Polska");
            builderEng.setDescription("Bla bla bla bla bla\nBla bla bla bla bla\nBla bla bla bla bla\n");
            builderEng.setThumbnail(EmbedSettings.THUMBNAIL_FLAG_ENG);
            builderEng.setImage(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessage(builderEng.build()).queue();
        });
    }
}
