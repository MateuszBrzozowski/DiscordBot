package pl.mbrzozowski.ranger.response;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import pl.mbrzozowski.ranger.helpers.Users;

import java.awt.*;

public class ResponseMessage {

    public static void youAreOnList(@NotNull ButtonInteractionEvent event) {
        event.reply("**Jesteś już na liscie**")
                .setEphemeral(true)
                .queue();
    }

    public static void youAreNotOnList(ButtonInteractionEvent event) {
        event.reply("**Nie jesteś zapisany na ten event**")
                .setEphemeral(true)
                .queue();
    }

    public static void youCantSignReserve(@NotNull ButtonInteractionEvent event) {
        event.reply("**Nie możesz wypisać się z głównej listy na rezerwową tuż przed rozpoczęciem eventu!**\n " +
                        "Jeżeli istnieje ryzyko, że się spóźnisz lub z ważnych powodów nie możesz uczestniczyć napisz wiadomość na tym kanale")
                .setEphemeral(true)
                .queue();
    }

    public static void youCantSingOut(@NotNull ButtonInteractionEvent event) {
        event.reply("**Nie możesz wypisać się z eventu tuż przed jego rozpoczęciem!**\n " +
                        "Jeżeli istnieje ryzyko, że się spóźnisz lub z ważnych powodów nie możesz uczestniczyć napisz wiadomość na tym kanale")
                .setEphemeral(true)
                .queue();
    }

    public static void eventIsBefore(@NotNull ButtonInteractionEvent event) {
        event.reply("Event już się wydarzył. Nie możesz się zapisać.")
                .setEphemeral(true)
                .queue();
    }

    public static void userHaveRecruitChannel(@NotNull ButtonInteractionEvent event) {
        event.reply("**Złożyłeś już podanie do naszego klanu i masz aktywny kanał rekrutacyjny!**")
                .setEphemeral(true)
                .queue();
    }

    public static void operationNotPossible(@NotNull ButtonInteractionEvent event) {
        event.reply("**Operacja niemożliwa do zrealizowania.**")
                .setEphemeral(true)
                .queue();
    }

    public static void cantCreateServerServiceChannel(@NotNull ButtonInteractionEvent event) {
        event.reply("""
                        Nie możesz utowrzyć kanału.
                        You can't create a ticket.

                        Prawdopodobnie masz już aktywny bilet. Jeśli go nie widzisz, skontaktuj się bezpośrednio z Adminem serwera.
                        Probably you have active ticket. If you can't see channel, please contact directly with Server Admin.""")
                .setEphemeral(true)
                .queue();
    }

    public static void maxRecruits(@NotNull ButtonInteractionEvent event) {
        event.reply("**REKRUTACJA DO KLANU RANGERS POLSKA TYMCZASOWO ZAMKNIĘTA!**")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkownik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Maksymalna liczba kanałów w kategorii StrefaRekruta.");
    }

    public static void userBlackList(@NotNull ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkownik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Jest na czarnej liście.");
    }

    /**
     * Wysyła do użytkownika o ID userID informację że jest już w klanie nie może złożyć podania na rekrutację.
     *
     * @param event ButtonInteractionEvent
     */
    public static void userIsInClanMember(@NotNull ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**\n" +
                        "Jesteś już w naszym klanie dzbanie!")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkownik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Jest już w naszym klanie.");
    }

    public static void recruitHasBeenAccepted(@NotNull ButtonInteractionEvent event) {
        event.reply("**Rekrut został już przyjęty**")
                .setEphemeral(true)
                .queue();
    }

    public static void noPermission(@NotNull ButtonInteractionEvent event) {
        event.reply("Brak uprawnień!")
                .setEphemeral(true)
                .queue();
    }

    public static void youCanCheckStatsOnChannel(@NotNull SlashCommandInteractionEvent event) {
        event.reply("You can check your stats on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">")
                .setEphemeral(true)
                .queue();
    }

    public static void youCanLinkedYourProfileOnChannel(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Use command /profile on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">")
                .setEphemeral(true)
                .queue();
    }

    public static void notConnectedAccount(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.BLACK);
        builder.setTitle("Your discord account isn't linked to your Steam profile.");
        builder.setDescription("""
                Link your discord account to your steam profile if you want view stats from our server via command **/profile**.

                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.\s
                */profile 76561197990543288*""");

        event.reply("").setEmbeds(builder.build()).setEphemeral(false).queue();
    }

    public static void cannotConnectStatsDB(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Can not connect to Stats database.\n" +
                "Please try again later.").setEphemeral(false).queue();
    }

    public static void connectSuccessfully(@NotNull SlashCommandInteractionEvent event) {
        event.reply("**Successfully**\n" +
                        "Your discord account is linked to your Steam profile.\n" +
                        "Now, You can use command **/stats**")
                .setEphemeral(false)
                .queue();
    }

    public static void connectUnSuccessfully(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.RED);
        builder.setTitle("Steam64ID is not valid");
        builder.setDescription("""
                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.\s
                */profile 76561197990543288*""");

        event.reply("")
                .setEmbeds(builder.build())
                .setEphemeral(false)
                .queue();
    }
}
