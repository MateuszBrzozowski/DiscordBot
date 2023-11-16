package pl.mbrzozowski.ranger.response;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;

@Slf4j
public class EmbedHelp extends EmbedCreator {

    private static PrivateChannel privateChannel;
    private static final String TITLE = "Ranger Bot - POMOC";
    private static final String RECRUIT = "recruit";
    public static final String REMINDER = "reminder";

    public static void help(String userID, String[] message) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            privateChannel = userById.openPrivateChannel().complete();
            boolean admin = Users.hasUserRole(userID, RoleID.RADA_KLANU);
            if (!admin) admin = Users.isUserDev(userID);

            log.info(Users.getUserNicknameFromID(userID) + " opened help.");

            if (message.length == 1) {
                mainHelp();
                if (Users.isUserDev(userID)) helpDevCommand();
            } else if (message.length == 2) {
                if (message[1].equalsIgnoreCase(RECRUIT)) {
                    if (admin) {
                        helpRecruit();
                    }
                } else if (message[1].equalsIgnoreCase(REMINDER)) {
                    helpReminder();
                }
            }
        }
    }

    private static void mainHelp() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(TITLE);
        builder.setFooter(getFooter());
        builder.addField("", ">>> **!help " + RECRUIT + "** - (Rada klanu) - Opis przycisków w kanałach rekrutacyjnych.\n" +
                "**!help " + REMINDER + "** - Przypomnienia dla eventów.\n", false);
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpDevCommand() {
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
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpReminder() {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT_HELP);
        builder.setTitle(TITLE + " - REMINDER");
        builder.setFooter(getFooter());
        builder.setDescription("""
                Możesz włączyć i wyłączyć powiadomienia dla eventów przy pomocy komend.

                **!reminder Off** - Wyłącza powiadomienia
                **!reminder On** - Włącza powiadomienia""");
        builder.setFooter("Komendy wpisywać w prywatnej wiadomości do bota.");
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    private static void helpRecruit() {
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
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }
}
