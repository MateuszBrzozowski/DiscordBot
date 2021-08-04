package embed;

import helpers.RangerLogger;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;

public class EmbedHelp {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static PrivateChannel privateChannel;
    private static String title = "Ranger Bot - POMOC";
    private static String footer = "RangerBot created by Brzozaaa © 2021";

    private static final String REKRUT = "recrut";
    private static final String GENERATOR = "generator";
    private static final String CHANNEL = "channel";
    private static final String REMINDER = "reminder";
    private static final String GAME = "game";
    private static final String SIGNIN = "zapisy";
    private static final String EVENT_SETTINGS = "event settings";

    private static void mainHelp() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        builder.setTitle(title);
        builder.setFooter(footer);
        builder.addField("", ">>> **!help " + REKRUT + "** - (Rada klanu) - Komendy do rekrutów. \n" +
                "**!help " + GENERATOR + "** - Automatyczny generator eventów.\n" +
                "**!help " + CHANNEL + "** - Tworzenie nowego kanału. Pomocne przy bardziej zaawansowanych zapisach z dłuższym, własnym opisem. \n" +
                "**!help " + SIGNIN + " cmd** - Tworzenie zapisów przy pomocy komend (zaawansowane)\n" +
                "**!help " + EVENT_SETTINGS + " - Zarządzanie eventami.\n" +
                "**!help " + REMINDER + "** - Przypomnienia dla eventów.\n" +
                "**!help " + GAME + "** - Gry", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    public static void help(String userID, String[] message) {
        JDA jda = Repository.getJda();
        privateChannel = jda.getUserById(userID).openPrivateChannel().complete();
        boolean admin = Users.hasUserRole(userID, RoleID.RADA_KLANU);
        if (!admin) admin = Users.isUserDev(userID);

        RangerLogger.info("Użytkownik [" + Users.getUserNicknameFromID(userID) + "] poprosił o pomoc. ");

        if (message.length == 1) {
            mainHelp();
            if (Users.isUserDev(userID)) helpDevCommand();
            RangerLogger.info("Wyświetlona pomoc główna");
        } else if (message.length == 2) {
            if (message[1].equalsIgnoreCase(REKRUT)) {
                if (admin) {
                    helpRecrut();
                    RangerLogger.info("Wyświetlona pomoc rekruci");
                }
            } else if (message[1].equalsIgnoreCase(GENERATOR)) {
                helpGenerator();
                RangerLogger.info("Wyświetlona pomoc generator");
            } else if (message[1].equalsIgnoreCase(REMINDER)) {
                helpReminder();
                RangerLogger.info("Wyświetlona pomoc reminder");
            } else if (message[1].equalsIgnoreCase(GAME)) {
                helpGame();
                RangerLogger.info("Wyświetlona pomoc game");
            } else if (message[1].equalsIgnoreCase(SIGNIN)) {
                helpSignInCommand();
                RangerLogger.info("Wyświetlona pomoc zapisy");
            } else if (message[1].equalsIgnoreCase(CHANNEL)) {
                helpNewChannel();
                RangerLogger.info("Wyświetlona pomoc nowy kanał");
            } else if (message[1].equalsIgnoreCase(EVENT_SETTINGS)) {
                helpEventsSettings();
                RangerLogger.info("Wyświetlona pomoc zarządzania eventami.");
            }
        } else if (message.length == 3) {
            if (message[1].equalsIgnoreCase(SIGNIN) && message[2].equalsIgnoreCase("cmd")) {
                helpSignInCommand();
                RangerLogger.info("Wyświetlona pomoc zapisy");
            }
        }
    }

    private static void helpEventsSettings() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - Zarządznie eventami");
        builder.setColor(Color.YELLOW);
        builder.setDescription("Każda z poniższych komend po poprawnym ich zastosowaniu wysyła do każdego zapisanego wiadomość prywatną z informacją o zmianach w evencie.");
        builder.addField("Komendy DEV", "**!time [msgID] [HH:mm]** - zmienia godzine w evencie\n" +
                "**!date [msgID] [dd.MM.yyyy]** - zmienia date w evencie\n", false);
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpDevCommand() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Ranger Bot - POMOC - DEV");
        builder.setColor(Color.RED);
        builder.addField("Komendy DEV", "**!disable [msgID]** -  Wyłącza buttony\n" +
                "**!disable [msgID] [channID]** -  Wyłącza buttony\n" +
                "**!enable [msgID]** - Włącza buttony\n" +
                "**!enable [msgID] [channID]** - Włącza buttony\n" +
                "**!deleteEvent [msgID]** - zamyka event i usuwa event z bazy danych.\n" +
                "**!status** - Wyswietla status aplikacji.", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpReminder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - REMINDER");
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        builder.setDescription("Dzień oraz godzinę przed każdym eventem rozsyłane są przypomnienia do każdego zapisanego użytkownika. Możesz je dla siebie wyłączyć i włączyć przy pomocy komend.\n\n" +
                "**!reminder Off** - Wyłącza powiadomienia\n" +
                "**!reminder On** - Włącza powiadomienia");
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpGame() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - GRY");
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        builder.addField("", ">>> **!kostka** - losuje i wyświetla wylosowną liczbę.\n" +
                "**!kostka <Temat_gry>** - Rozpoczyna grę na kanale na którym zostało wpisane polecenie. Gra na dwie osoby. Osoba z większą liczbą wygrywa.", false);
        builder.setFooter("Komendy można wpisać na dowolnym kanale na discordzie Rangers Polska.");
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpSignInCommand() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle(title + " - ZAPISY Komendy (zaawansowane)");
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.addField("ZAPISY/LISTA podstawowa", "\n>>> **!zapisy <nazwa> <data> <godzina>** - tworzy kanał i na nim listę.\n**!zapisyhere <nazwa> <data> <godzina>** - tworzy listę na kanale na którym się znajdujemy. " +
                "Użyteczne przy tworzeniu w pierwszej kolejności kanału dla eventu, a potem jeżeli chcemy dodać listę. (Nowy kanał) \n\n " +
                "Polecenie !zapisy wpisujemy na dowolnym kanale lub w prywatnej wiadomości do bota.\n" +
                "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                "Nazwa eventu jednoczłonowa bez opisu zawartego na liście. \n**UWAGA** Kolejność parametrów ma znaczenie! \n" +
                "Dodatkowo możemy dodać tylko jeden parametr na końcu komendy.\n" +
                "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                "defaultowo widzi i pinguje Clan Memberów.\n\n" +
                "(przykład: !zapisy CCFN 19.06.2021 19:30)\n" +
                "(przykład: !zapisy CCFN 19.06.2021 19:30 -ac)\n" +
                "(przykład: !zapisy CCFN 19.06.2021 19:30 -r)\n\n", false);
        builder.addField("ZAPISY/LISTA zaawansowana z tworzeniem kanału", "\n>>> **!zapisy \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                "Polecenie wpisujemy na dowolnym kanale lub w prywatnej wiadomości do bota.\n" +
                "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                "Otwiera nowy kanał, tworzy listę. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                "Dodatkowe parametry:\n" +
                "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                "Defaultowo widzi i pinguje Clan Memberów.\n" +
                "Maksymalna liczba znaków:\n" +
                "Nazwa eventu - 256\n" +
                "Tekst (opis eventu) - 2048 - Jeżeli chcesz wpisać własnoręcznie lub twój opis będzie dłuższy patrz **Nowy kanał**\n\n" +
                "(przykład: !zapisy -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);
        builder.addField("ZAPISY/LISTA zaawansowana na kanale.", "\n>>> **!zapisyhere \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                "Tworzy listę na mecz na kanale na którym się znajdujemy. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                "Dodatkowe parametry:\n" +
                "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                "**-c** kanał widoczny dla Clan Member, Pinguje tylko Clan Memberów.\n" +
                "defaultowo lista tworzy się bez pingowania żadnej roli nie nadpisując uprawnień kanału.\n" +
                "Maksymalna liczba znaków:\n" +
                "Nazwa eventu - 256\n" +
                "Tekst (opis eventu) - 2048\n\n" +
                "(przykład: !zapisyhere -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpNewChannel() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - NOWY KANAŁ");
        builder.setColor(Color.YELLOW);
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.addField("Tworzymy gdy opis eventu jest zbyt długi by zmieścić go na liście.", ">>> **!newChannel** - Tworzy nowy kanał.\n" +
                "*(Polecenie możesz wpisać tutaj w prywatnej wiadomości lub na dowolnym kanale)*\n" +
                "**!name <nazwa>** - wpisz na nowo utworzonym kanale aby zmienić nazwe kanału (najlepiej nazwa eventu)\n\n" +
                "Następnie wpisz swój opis i użyj komendy !generatorhere", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpGenerator() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - GENERATOR");
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        builder.addField("Po wpisaniu poniższych komend uruchamia się generator eventu. Postępuj zgodnie z instrukcjami.",
                ">>> **!generator** - Tworzy kanał i listę w sekcji mecze/szkolenia/eventy\n" +
                        "*(Polecenie możesz napisać tutaj w prywatnej wiadomości lub na dowolnym kanale)*\n" +
                        "**!generatorHere** - Tworzy listę na kanale w którym polecenie zostalo wpisane. \n" +
                        "Używać gdy opis eventu jest zbyt długi i nie zmieści się bezpośrednio na liście. (Maksymalna liczba znaków - 2048)\n" +
                        "Stwórz kanał, dodaj swój opis, a następnie wywołaj tą komendę. " +
                        "*(POMOC - Nowy kanał - !help channel)*\n\n", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    private static void helpRecrut() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title + " - REKRUT");
        builder.setFooter(footer);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setColor(Color.YELLOW);
        builder.addField("", "\n>>> **!p** - Pinguje rekruta i wysyła na kanale POZYTYWNY wynik rekrutacji\n" +
                "**!n** - Pinguje rekruta i wysyła na kanale NEGATYWNY wynik rekrutacji\n" +
                "**!close** - Zamyka kanał rekrutacji - rekrut nie widzi kanału/nie może pisać.\n" +
                "**!open** - Otwiera kanał rekrutacji - rekrut ponownie może widzieć i pisać na kanale.\n" +
                "**!remove** - Usuwa kanał rekrutacji. Możesz usunąć kanał ręcznie bez komendy.\n\n" +
                "", false);
        privateChannel.sendMessage(builder.build()).queue();
    }

    public static void infoEditEventChannel(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setFooter(footer);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setTitle("Tworzenie eventu na kanale - POMOC");
            builder.addField("", "Jeżeli potrzebujesz dodaj swój opis na kanale eventu, a następnie stwórz listę " +
                    "przy pomocy poniższych komend", false);
            builder.addField("Wszystkie komendy wpisuj na kanale eventu", "**!name <nazwa>** - zmienia nazwę kanału\n" +
                    "**!generatorHere** - uruchamia generator tworzenia eventów\n" +
                    "lub stwórz listę bez generatora używając komendy **!zapisyhere** <- więcej informacji jak tworzyć " +
                    "listę w pomocy bota pod komendą **!help**", false);
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

}
