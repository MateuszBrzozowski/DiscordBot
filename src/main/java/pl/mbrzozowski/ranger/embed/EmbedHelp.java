package pl.mbrzozowski.ranger.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;

public class EmbedHelp extends EmbedCreator {

    private static PrivateChannel privateChannel;
    private static final String title = "Ranger Bot - POMOC";
    private static final String REKRUT = "recrut";
    private static final String GENERATOR = "generator";
    private static final String REMINDER = "reminder";
    private static final String GAME = "game";
    private static final String EVENT_SETTINGS = "event";

    private static void mainHelp() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title);
        builder.setFooter(getFooter());
        builder.addField("", ">>> **!help " + REKRUT + "** - (Rada klanu) - Komendy do rekrutów. \n" +
                "**!help " + GENERATOR + "** - Automatyczny generator eventów.\n" +
                "**!help " + EVENT_SETTINGS + "** - (Rada klanu) - Zarządzanie eventami.\n" +
                "**!help " + REMINDER + "** - Przypomnienia dla eventów.\n" +
                "**!help " + GAME + "** - Gry", false);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void help(String userID, String[] message) {
        User userById = Repository.getJda().getUserById(userID);
        if (userById != null) {
            privateChannel = userById.openPrivateChannel().complete();
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
                } else if (message[1].equalsIgnoreCase(EVENT_SETTINGS)) {
                    if (admin) {
                        helpEventsSettings();
                        RangerLogger.info("Wyświetlona pomoc zarządzania eventami.");
                    }
                }
            }
        }
    }

    private static void helpEventsSettings() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title + " - Zarządznie eventami");
        builder.setDescription("Niektóre z poniższych komend po poprawnym ich zastosowaniu wysyłają do każdego zapisanego wiadomość prywatną z informacją o zmianach w evencie.");
        builder.addField("", """
                **!time [msgID] [HH:mm]** - zmienia godzine w evencie
                **!time [msgID] [HH:mm] -noNotifi** - zmienia godzine w evencie bez powiadomienia uczestników
                **!date [msgID] [dd.MM.yyyy]** - zmienia date w evencie
                **!date [msgID] [dd.MM.yyyy] -noNotifi** - zmienia date w evencie bez powiadomienia uczestników
                **!cancelEvent [msgID]** - Zamyka Event i usuwa z bazy - OSTROŻNIE! Nie będzie możliwości powrotu. Bądź pewny tego ruchu. Wysyła powiadomienia do każdego uczestnika o odwołaniu.
                **!cancelEvent [msgID] -noNotifi** - Jak wyżej bez powiadomienia o odwołaniu.""", false);
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpDevCommand() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Ranger Bot - POMOC - DEV");
        builder.addField("Komendy DEV", """
                **!disable [msgID]** -  Wyłącza buttony
                **!disable [msgID] [channID]** -  Wyłącza buttony
                **!enable [msgID]** - Włącza buttony
                **!enable [msgID] [channID]** - Włącza buttony
                **!status** - Wyswietla status aplikacji.
                **!msg [channelID]** - Wysyła wiadomość na kanale o ID [channelID] jako bot
                **!msgCancel** - Anuluje wysyłanie wiadomości jako bot.
                **!removeUserFromEvent [USER_ID] [EVENT_ID]** - Wykreśla użytkownika z eventu.
                **!removeUserFromEvents [USER_ID]** - Wykreśla użytkownika ze wszystkich eventów.""", false);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpReminder() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title + " - REMINDER");
        builder.setFooter(getFooter());
        builder.setDescription("""
                Godzinę przed każdym eventem rozsyłane są przypomnienia do każdego zapisanego użytkownika. Możesz je dla siebie wyłączyć i włączyć przy pomocy komend.

                **!reminder Off** - Wyłącza powiadomienia
                **!reminder On** - Włącza powiadomienia""");
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpGame() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title + " - GRY");
        builder.addField("", ">>> **!kostka** - losuje i wyświetla wylosowną liczbę.\n" +
                "**!kostka <Temat_gry>** - Rozpoczyna grę na kanale na którym zostało wpisane polecenie. Gra na dwie osoby. Osoba z większą liczbą wygrywa.", false);
        builder.setFooter("Komendy można wpisać na dowolnym kanale na discordzie Rangers Polska.");
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpGenerator() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title + " - GENERATOR");
        builder.addField("Po wpisaniu poniższych komend uruchamia się generator eventu. Postępuj zgodnie z instrukcjami.",
                """
                        >>> **!generator** - Tworzy kanał i listę w sekcji mecze/szkolenia/eventy
                        *(Polecenie możesz napisać tutaj w prywatnej wiadomości lub na dowolnym kanale)*
                        **!generatorHere** - Tworzy listę na kanale w którym polecenie zostalo wpisane.\s
                        Używać gdy opis eventu jest zbyt długi i nie zmieści się bezpośrednio na liście. (Maksymalna liczba znaków - 2048)
                        Stwórz kanał, dodaj swój opis, a następnie wywołaj tą komendę. *(POMOC - Nowy kanał - !help channel)*

                        """, false);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpRecrut() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(title + " - REKRUT");
        builder.addField("", """

                >>> **!p** - Pinguje rekruta i wysyła na kanale POZYTYWNY wynik rekrutacji
                **!n** - Pinguje rekruta i wysyła na kanale NEGATYWNY wynik rekrutacji
                **!close** - Zamyka kanał rekrutacji - rekrut nie widzi kanału/nie może pisać.
                **!open** - Otwiera kanał rekrutacji - rekrut ponownie może widzieć i pisać na kanale.
                **!remove** - Usuwa kanał rekrutacji. Możesz usunąć kanał ręcznie bez komendy.

                """, false);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }
}
