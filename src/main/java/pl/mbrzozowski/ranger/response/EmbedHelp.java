package pl.mbrzozowski.ranger.response;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;

@Slf4j
public class EmbedHelp extends EmbedCreator {

    private static final String TITLE = "Ranger Bot - POMOC";
    private static final String RECRUIT = "recruit";
    public static final String REMINDER = "reminder";

    public static void help(User user, String contentRaw) {
        if (user != null) {
            log.info(user + " - open help with command: {}", contentRaw);
            String[] words = contentRaw.split(" ");
            boolean admin = Users.hasUserRole(user.getId(), RoleID.CLAN_COUNCIL);
            if (!admin) admin = Users.isDev(user.getId());

            if (words.length == 1) {
                mainHelp(user);
                if (Users.isDev(user.getId())) helpDevCommand(user);
            } else if (words.length == 2) {
                if (words[1].equalsIgnoreCase(RECRUIT)) {
                    if (admin) {
                        helpRecruit(user);
                    }
                } else if (words[1].equalsIgnoreCase(REMINDER)) {
                    helpReminder(user);
                }
            }
        }
    }

    private static void mainHelp(@NotNull User user) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(TITLE);
        builder.setFooter(getFooter());
        builder.addField("", ">>> **!help " + RECRUIT + "** - (Rada klanu) - Opis przycisków w kanałach rekrutacyjnych.\n" +
                "**!help " + REMINDER + "** - Przypomnienia dla eventów.\n", false);
        user.openPrivateChannel().queue(privateChannel -> privateChannel
                .sendMessageEmbeds(builder.build())
                .queue(m -> log.info(user + " - main help opened")));
    }

    private static void helpDevCommand(@NotNull User user) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Ranger Bot - POMOC - DEV");
        builder.addField("Komendy DEV", """
                **!disable [msgID]** -  Wyłącza buttony
                **!disable [msgID] [channelID]** -  Wyłącza buttony
                **!enable [msgID]** - Włącza buttony
                **!enable [msgID] [channelID]** - Włącza buttony
                **!status** - Wyświetla status aplikacji.
                **!msg [channelID]** - Wysyła wiadomość na kanale o ID [channelID] jako bot
                **!msgCancel** - Anuluje wysyłanie wiadomości jako bot.
                **!removeUserFromEvent [USER_ID] [EVENT_ID]** - Wykreśla użytkownika z eventu.
                **!removeUserFromEvents [USER_ID]** - Wykreśla użytkownika ze wszystkich eventów.""", false);
        user.openPrivateChannel().queue(privateChannel -> privateChannel
                .sendMessageEmbeds(builder.build())
                .queue(m -> log.info(user + " - dev help opened")));
    }

    private static void helpReminder(@NotNull User user) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(TITLE + " - REMINDER");
        builder.setFooter(getFooter());
        builder.setDescription("""
                Możesz włączyć i wyłączyć powiadomienia dla eventów przy pomocy komend.

                **!reminder Off** - Wyłącza powiadomienia
                **!reminder On** - Włącza powiadomienia""");
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        user.openPrivateChannel().queue(privateChannel -> privateChannel
                .sendMessageEmbeds(builder.build())
                .queue(m -> log.info(user + " - reminder help opened")));
    }

    private static void helpRecruit(@NotNull User user) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(TITLE + " - REKRUT");
        builder.addField("Niebieski przycisk",
                "Użytkownik przeszedł rozmowę. Rozpoczął się jego okres rekrutacyjny. Dodaje clan tag do nickname." +
                        "Nadaje rangę rekruta. **(UWAGA! Sprawdzić osobiście tą zmiany).**",
                false);
        builder.addField("Szary przycisk",
                "Odrzuca podanie. Wysyła potwierdzenie zdarzenia na kanale. Najlepiej używać jeżeli użytkownik " +
                        "nie rozpoczął rekrutacji, nie przeszedł rozmowy lub wyszedł z discorda.",
                false);
        builder.addField("Zielony przycisk",
                "Pinguje rekruta i wysyła na kanale POZYTYWNY wynik rekrutacji. Zmienia nickname bez małego r. " +
                        "Odbiera rangę rekruta i nadaje Clan Member" +
                        " **(UWAGA! Sprawdzić osobiście tą zmiany).**",
                false);
        builder.addField("Czerwony przycisk",
                "Pinguje rekruta i wysyła na kanale NEGATYWNY wynik rekrutacji. Usuwa clan tag z nickname. " +
                        "Odbiera rangę rekruta **(UWAGA! Sprawdzić osobiście tą zmiany).**",
                false);
        user.openPrivateChannel().queue(privateChannel -> privateChannel
                .sendMessageEmbeds(builder.build())
                .queue(m -> log.info(user + " - recruit help opened")));
    }
}
