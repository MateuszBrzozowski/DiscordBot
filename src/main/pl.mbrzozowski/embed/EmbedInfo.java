package embed;

import event.Event;
import event.EventChanges;
import helpers.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;
import recrut.Recruits;
import stats.MapLayer;

import java.awt.*;
import java.util.List;

public class EmbedInfo extends EmbedCreator {

    protected static final Logger logger = LoggerFactory.getLogger(EmbedInfo.class.getName());

    public static void recruiter(MessageReceivedEvent event) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("PODANIE");
        builder.addField("", "Chcemy nasze wieloletnie doświadczenie przekazać kolejnym Rangersom. Nasza gra opiera się na wzajemnej komunikacji i skoordynowanym działaniu. " +
                "Jako grupa, pielęgnujemy dobrą atmosferę i przyjazne, dojrzałe relacje między członkami naszego klanu, a także polską społecznością. \n", false);
        builder.addField("Złóż podanie do klanu klikając przycisk PONIŻEJ", "", false);
        event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.NEW_RECRUT, "Podanie")).queue();
    }

    /**
     * Wysyła informację do użytkownika o userID, że jest już zapisany na liście eventu.
     *
     * @param userID ID użytkownika
     */
    public static void cantSignIn(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Jesteś już na liście");
            builder.setDescription("Jesteś już na głównej liście w meczu na który próbowałeś się zapisać.");
            builder.addField("", "Jeżeli nie widzisz siebie na liście, nie możesz się zapisać bo otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Jesteś już na liście rezerwowej");
            builder.setDescription("Jesteś już na rezerwowej liście w meczu na który próbowałeś się zapisać.");
            builder.addField("", "Jeżeli nie widzisz siebie na liście, nie możesz się zapisać bo otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Nie możesz wypisać się z tego meczu.");
            builder.setDescription("Nie możesz wypisać się z meczu na który się nie zapisałeś!");
            builder.addField("", "Jeżeli jednak jesteś na liście a nadal otrzymujesz tą wiadomość. Napisz do administracji.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
            builder.setTitle("Podałeś nielogiczną datę i czas. Event w przeszłości?");
            builder.setDescription("Podaj prawidłową datę i czas.");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    /**
     * Wyświetla informację, że kanał podany w parametrze został zamknięty.
     *
     * @param userID  ID użytkownika który zamyka kanał
     * @param channel Kanał który został zamknięty.
     */
    public static void closeChannel(String userID, MessageChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Kanał zamknięty");
        builder.setDescription("Kanał zamknięty przez " + Users.getUserNicknameFromID(userID) + ".");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.danger(ComponentId.REMOVE, "Usuń kanał"))
                .queue();
    }

    public static void confirmCloseChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.QUESTION);
        builder.setTitle("Do you want close the ticket?");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.success("closeYes", "Yes"),
                        Button.danger("closeNo", "No"))
                .queue();
    }

    public static void confirmRemoveChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.QUESTION);
        builder.setTitle("Potwierdź czy chcesz usunąć kanał?");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.success(ComponentId.REMOVE_YES, "Tak"),
                        Button.danger(ComponentId.REMOVE_NO, "Nie"))
                .queue();
    }

    /**
     * Wyświetla informację, że kanał podany w parametrze został otwarty.
     *
     * @param userID  ID użytkownika który zamyka kanał
     * @param channel Kanał który został otwarty.
     */
    public static void openChannel(String userID, TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("Kanał otwarty");
        builder.setDescription("Kanał otwarty przez " + Users.getUserNicknameFromID(userID) + ".");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    /**
     * Wyświetla informację że kanał został usunięty i że za chwilę zniknie.
     *
     * @param channel Kanał który został usunięty.b
     */
    public static void removedChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("Kanał usunięty.");
        channel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
            builder.setTitle(EmbedSettings.RESULT + "NEGATYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(userID));
            channel.sendMessage("<@" + recruits.getRecruitIDFromChannelID(channel.getId()) + ">").setEmbeds(builder.build()).queue();
            String oldName = channel.getName();
            channel.getManager().setName(EmbedSettings.RED_CIRCLE + oldName).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
            builder.setTitle(EmbedSettings.RESULT + "POZYTYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(userID));
            channel.sendMessage("Gratulacje <@" + recruits.getRecruitIDFromChannelID(channel.getId()) + ">").setEmbeds(builder.build()).queue();
            String oldName = channel.getName();
            channel.getManager().setName(EmbedSettings.GREEN_CIRCLE + oldName).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ WIĘCEJ NIŻ JEDNO PODANIE!");
            builder.setDescription("Zlożyłeś już podanie do naszego klanu i\n" +
                    "jesteś w trakcie rekrutacji.\n");
            builder.addField("Jeżeli masz pytania w związku z Twoją rekrutacją", "", false);
            builder.addField("1. Spradź kanały", "Znajdź kanał przypisany do twojej rekrutacji i napisz do nas.", false);
            builder.addField("2.Nie widze kanału.", "Jeżeli nie widzisz kanału przypisanego do twojej rekrutacji " +
                    "skontaktuj się z Drill Instrutor. Znajdziesz ich po prawej stronie na liście użytkowników.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setDescription("Jesteś już w naszym klanie dzbanie!");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
            RangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Jest już w naszym klanie.");
        });
    }

    public static void maxRecrutis(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("REKRTUACJA TYMCZASOWO ZAMKNIĘTA!");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
            RangerLogger.info("Użytkonik [" + jda.getUserById(userID).getName() + "] chciał złożyć podanie. Maksymalna liczba kanałów w kategorii StrefaRekruta.");
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!");
            builder.setDescription("Masz przypisaną rolę innego klanu na naszym discordzie.");
            builder.addBlankField(false);
            builder.addField("- Nie należę do żadnego klanu", "Proszę znajdź użytkownika z rolą Rada klanu na naszym discordzie i " +
                    "napisz do nas.", false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
            builder.setTitle("MIAŁEŚ AKTYWNY GENERATOR");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
            builder.setTitle("OTWIERAM NOWY GENERATOR");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    /**
     * Jeżeli użytkownik nie jest botem usuwa wiadomość i wysyła informację że kanał służy do logowania i tam nie piszemy.
     *
     * @param event Wydarzenie napisania wiadomości na kanale tekstowym.
     */
    public static void noWriteOnLoggerChannel(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getMessage().delete().submit();
            JDA jda = Repository.getJda();
            jda.getUserById(event.getAuthor().getId()).openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
                builder.setTitle("Zachowajmy porządek!");
                builder.setDescription("Panie administratorze! Zachowajmy czystość na kanale do loggowania. Proszę nie wtrącać się w moje wypociny.");
                builder.setFooter(getFooter());
                privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Event już się wydarzył. Nie możesz się zapisać.");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Nie możesz wypisać się z eventu tuż przed jego rozpoczęciem!");
            builder.setDescription("Jeżeli nie możesz pojawić się z ważnych przyczyn przekaż informację na kanale eventu dlaczego Cię nie będzie");
            String linkToEventChannel = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" +
                    event.getChannelID(eventID) + ")";
            builder.addField("Link do eventu", linkToEventChannel, false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Nie możesz wypisać się z głownej listy na rezerwową tuż przed rozpoczęciem eventu!");
            builder.setDescription("Jeżeli istnieje ryzyko, że się spóźnisz powiadom nas na kanale eventu");
            String linkToEventChannel = "[" + event.getEventNameFromEmbed(eventID) + "](https://discord.com/channels/" +
                    CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" +
                    event.getChannelID(eventID) + ")";
            builder.addField("Link do eventu", linkToEventChannel, false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
            builder.setTitle("Przypomnienia wyłączone.");
            builder.setDescription("Aby włączyć ponownie przypomnienia użyj komendy **!reminder On**");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
            builder.setTitle("Przypomnienia włączone.");
            builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !help Reminder");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    /**
     * Wysyła do użytkownika o ID userID wiadomość o zmianach w evencie.
     *
     * @param userID     ID użytkownika
     * @param eventID    ID eventu
     * @param whatChange jaka zmiana zostala wykonana
     * @param dateTime   data i czas
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("**UWAGA:** Zmiany w wydarzeniu.");
            builder.setDescription(finalDescription);
            builder.addField("Szczegóły eventu", link + "\n:date: " + dateTime, false);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
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
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
            builder.setTitle("GENEROWANIE LISTY ZOSTAŁO PRZERWANE");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    public static void cancelEventEditing(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle("Edytor zamknięty");
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    public static void seedersRoleJoining(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("SQUAD SERVER SEEDER");
        builder.addField("", "Jeśli chcesz pomóc nam w rozkręcaniu naszego serwera. Możesz przypisać sobię rolę klikając w poniższy przycisk by otrzymywać ping. \n" +
                "**Dodatkowo każdy seeder otrzyma whitelistę na nasz serwer.**", false);
        builder.addField("", "If you would like to help us seed our server you can add role below to receive a ping. \n" +
                "**All seeders will recive whitelist.**", false);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.SEED_ROLE, "Add/Remove Seed Role ").withEmoji(Emoji.fromUnicode("\uD83C\uDF31"))).queue();
    }

    /**
     * Formatka z opisem jak stworzyć ticket.
     *
     * @param channel Kanał na którym wstawiana jest formatka.
     */
    public static void serverService(MessageChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Rangers Polska Servers - Create Ticket :ticket:");
        builder.addField("", "Jeśli potrzebujesz pomocy admina naszych serwerów, " +
                "kliknij w odpowiedni przycisk poniżej.", false);
        builder.addField("--------------------", "If you need help of Rangers Polska Servers Admins, " +
                "please react with the correct button below.", false);
        channel.sendMessageEmbeds(builder.build()).setActionRow(
                Button.primary(ComponentId.SERVER_SERVICE_REPORT, "Report Player").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_RED)),
                Button.primary(ComponentId.SERVER_SERVICE_UNBAN, "Unban appeal").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_BLUE)),
                Button.primary(ComponentId.SERVER_SERVICE_CONTACT, "Contact With Admin").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_GREEN))).queue();
    }

    /**
     * Wysyła do użytkownika prywatną wiadomość, że nie może utworzyć nowego ticketu
     *
     * @param userID ID użytkownika do którego wysyłana jest prywatna wiadomość
     */
    public static void cantCreateServerServiceChannel(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
            builder.setTitle("Nie możesz utowrzyć kanału.\nYou can't create a ticket.");
            builder.setDescription("Prawdopodobnie masz już aktywny bilet. Jeśli go nie widzisz, skontaktuj się bezpośrednio z Adminem serwera.\n\n" +
                    "Probably you have active ticket. If you can't see channel, please contact directly with Server Admin.");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedReport(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
        builder.setTitle("Report player");
        builder.addField("", "Zgłoś gracza według poniższego formularza.\n\n" +
                "1. Podaj nick.\n" +
                "2. Opisz sytuację i podaj powód dlaczego zgłaszasz gracza.\n" +
                "3. Podaj nazwę serwera.\n" +
                "4. Dodaj dowody. Screenshot lub podaj link do wideo (np. Youtube).", false);
        builder.addField("--------------------", "Report player according to the form below.\n\n" +
                "1. Player nick.\n" +
                "2. Describe of bad behaviour.\n" +
                "3. Server name.\n" +
                "4. Add evidence. Screenshot or video link (e.g. Youtube).", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary("close", "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedUnban(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLUE, ThumbnailType.DEFAULT);
        builder.setTitle("Unban player");
        builder.addField("", "Napisz tutaj jeżeli chcesz odowłać swój ban.\n" +
                "1. Podaj swój nick i/lub steamid.\n" +
                "2. Podaj nazwę serwera.", false);
        builder.addField("--------------------", "Write here if you want to revoke your ban.\n" +
                "1. Provide your ingame nick and/or steamid.\n" +
                "2. Server name.", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary("close", "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedContact(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.GREEN, ThumbnailType.DEFAULT);
        builder.setTitle("Contact with Admin");
        builder.addField("", "Napisz tutaj jeżeli masz jakiś problem z którymś z naszych serwerów, dodaj screenshoty, nazwę serwera. " +
                "Twój nick w grze lub/i steamId64.", false);
        builder.addField("--------------------", "Please describe your problem with more details, " +
                "screenshots, servername the issue occured on and related steamId64", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary("close", "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    public static void notConnectedAccount(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK, ThumbnailType.DEFAULT);
        builder.setTitle("Your discord account isn't linked to your Steam profile.");
        builder.setDescription("If you want to link your discord account to your steam account use the command **!profile <steam64ID>**\n\n" +
                "Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/\n\n" +
                "e.g. \n*!profile 76561197990543288*");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void connectSuccessfully(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("Successfully");
        builder.setDescription("Your discord account is linked to your Steam profile.\n" +
                "You can use command **!stats** to view your statistic from our server.");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void connectUnSuccessfully(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Steam64ID is not valid");
        builder.setDescription("Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/\n\n" +
                "e.g. \n*!profile 76561197990543288*");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void youCanCheckStatsOnChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setDescription("You can check your stats on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void noDataToShow(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setTitle("No data to show.");
        builder.setDescription("If you played on our server and there is no data, please check your Steam64ID and update it by command !profile.\n\n" +
                "Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/\n\n" +
                "e.g. \n*!profile 76561197990543288*");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void youCanLinkedYourProfileOnChannel(TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setDescription("Use command !profile on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">");
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void noActiveEvents(TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Brak aktywnych eventów");
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void recrutOpinionsFormOpening(MessageReceivedEvent messageReceived) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Rekrut opinie");
        builder.addField("Otwórz formularz by wystawić opinię na temat rekruta.", "", false);
        messageReceived.getTextChannel().sendMessageEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.OPEN_FORM, "Otwórz formularz"))
                .queue();
    }

    public static void showLastTenMaps(List<MapLayer> mapLayers, MessageReceivedEvent event) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setTitle("Ostatnie 10 map (min. 5 graczy)");
        String mapsString = "";
        for (int i = 0; i < mapLayers.size(); i++) {
            mapsString += i + 1 + ". **" + mapLayers.get(i).getName() + "** - " + mapLayers.get(i).getLayerName() + "\n";
        }
        builder.setDescription(mapsString);
        event.getTextChannel().sendMessageEmbeds(builder.build()).queue();
    }

    public static void recruitAccepted(String userName, TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Przyjęty");
        builder.setDescription("Przyjęty na rekrutację przez: " + userName);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void recruitWhiteListInfo(TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Whitelista");
        builder.setDescription("Uzupełnij przesłany poniżej formularz aby dostać whitelistę na naszym " +
                "serwerze oraz adminkę na serwery treningowe abyś mógł swobodnie trenować swoje umiejętności w grze - " +
                "więcej info o serwerach treningowych tutaj <#841233590384853012>");
        builder.addField(
                "Formularz",
                "https://docs.google.com/forms/d/e/1FAIpQLSeCl7SdQ_TwtmwjJMPmLiNCFKQHx0xEly_eIcvuXlSQO_VMEQ/viewform?usp=sf_link",
                false);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void sendRoles(MessageReceivedEvent messageReceived) {
        SelectMenu roles = RoleID.getRoleToSelectMenu();
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Role na discordzie");
        builder.setDescription("Przypisz rolę wybierająć ją z poniższej listy.");
        messageReceived.getTextChannel()
                .sendMessageEmbeds(builder.build())
                .setActionRow(roles)
                .queue();
    }
}
