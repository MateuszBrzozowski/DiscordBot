package pl.mbrzozowski.ranger.response;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.giveaway.Giveaway;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.members.clan.rank.Rank;

import java.util.List;

@Slf4j
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

    public static void listIsFull(@NotNull ButtonInteractionEvent event) {
        event.reply("Brak miejsc. Lista pełna.")
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
                .queue(m -> log.info("{} - user have recruit channel", event.getUser()));
    }

    public static void noReqTimeOnServer(@NotNull ButtonInteractionEvent event) {
        event.reply("**Odczekaj 10 minut od dołączenia na serwer zanim złożysz podanie**")
                .setEphemeral(true)
                .queue(m -> log.info("{} - no time req on server", event.getUser()));
    }

    public static void operationNotPossible(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - operation not possible");
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

    public static void userBlackList(@NotNull ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**")
                .setEphemeral(true)
                .queue(m -> log.info("{} - user on black list", event.getUser()));
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
                .queue(m -> log.info("{} - user is already clan member", event.getUser()));
    }

    public static void recruitHasBeenAccepted(@NotNull ButtonInteractionEvent event) {
        event.reply("**Rekrut został już przyjęty na rekrutacje. Jeżeli chcesz możesz dać wynik rekrutacji używając przycisku zielonego lub czerwonego.**")
                .setEphemeral(true)
                .queue(m -> log.info("{} - recruit(channelId={}) has been accepted", event.getUser(), event.getChannel().getId()));
    }

    public static void recruitHasBeenRejected(@NotNull ButtonInteractionEvent event) {
        event.reply("**Rekrut z wynikiem rekrutacji. Operacja niemożliwa do zrealizowania.**")
                .setEphemeral(true)
                .queue(m -> log.info("{} - recruit(channelId={}) has been rejected", event.getUser(), event.getChannel().getId()));
    }

    public static void noPermission(@NotNull ButtonInteractionEvent event) {
        log.info(event.getUser() + " - No permission");
        event.reply("Brak uprawnień!")
                .setEphemeral(true)
                .queue();
    }

//    public static void youCanCheckStatsOnChannel(@NotNull SlashCommandInteractionEvent event) {
//        event.reply("You can check your stats on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">")
//                .setEphemeral(true)
//                .queue();
//    }

//    public static void youCanLinkedYourProfileOnChannel(@NotNull SlashCommandInteractionEvent event) {
//        event.reply("Use command /profile on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">")
//                .setEphemeral(true)
//                .queue();
//    }

    public static void notConnectedAccount(@NotNull SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("""
                **If you want check stats you have to link discord account to steam profile via command /profile**
                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.
                */profile 76561197990543288*""").setSuppressEmbeds(true).queue();
    }

    public static void cannotConnectStatsDB(@NotNull SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("Can not connect to Stats database.\n" +
                "Please try again later.").queue();
    }

    public static void connectSuccessfully(@NotNull SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("""
                **Successfully**
                Your discord account is linked to your Steam profile.
                Now, You can use command **/stats**""").queue();
    }

    public static void connectUnSuccessfully(@NotNull SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("""
                **Steam64ID is not valid**
                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.
                */profile 76561197990543288*""").setSuppressEmbeds(true).queue();
    }

    public static void playerStatsIsNull(@NotNull SlashCommandInteractionEvent event) {
        log.warn("User can not be found in the database");
        event.getHook().editOriginal("User can not be found in the database").queue();
    }

    public static void giveawayUnexpectedException(@NotNull ButtonInteractionEvent event) {
        event.reply("Wystąpił nieoczekiwany błąd.\n" +
                        "Spróbuj ponownie później lub skontaktuj się z administracją <@&" + RoleID.CLAN_COUNCIL + ">")
                .setEphemeral(true)
                .queue();
    }

    public static void giveawayNoExist(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Giveaway o podanym ID nie istnieje. " +
                        "Spróbuj ponownie lub zgłoś problem <@" + RoleID.DEV_ID + ">")
                .setEphemeral(true)
                .queue();
    }

    public static void giveawayAdded(@NotNull ButtonInteractionEvent event) {
        event.reply("Gratulacje! Twój zapis na giveaway został właśnie zarejestrowany poprawnie! " +
                        "Życzę powodzenia i trzymam kciuki za Twoją wygraną!")
                .setEphemeral(true)
                .queue();
    }

    public static void giveawayUserExist(@NotNull ButtonInteractionEvent event) {
        event.reply("Wygląda na to, że już jesteś zapisany na nasz giveaway")
                .setEphemeral(true)
                .queue();
    }

    public static void giveawayClanMemberExclude(@NotNull ButtonInteractionEvent event) {
        event.reply("Sorry, ale wygląda na to, że losowanie nie przyjmuje zgłoszeń od osób, których szczęście poszło na długą kawę!")
                .setEphemeral(true)
                .queue();
    }

    public static void noGiveaways(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Brak giveawayów.").setEphemeral(true).queue();
    }

    public static void moreThanOneGiveaway(@NotNull SlashCommandInteractionEvent event, List<Giveaway> activeGiveaways) {
        event.reply("Więcej niż jeden giveaway. Użyj ponownie komendy i uzupełnij id\n"
                        + getDescriptionOfActiveGiveaways(activeGiveaways))
                .setEphemeral(true)
                .queue();
    }

    @NotNull
    private static String getDescriptionOfActiveGiveaways(@NotNull List<Giveaway> giveaways) {
        StringBuilder builder = new StringBuilder();
        for (Giveaway giveaway : giveaways) {
            builder
                    .append("**ID=")
                    .append(giveaway.getId())
                    .append("** - ")
                    .append("https://discord.com/channels/")
                    .append(CategoryAndChannelID.RANGERSPL_GUILD_ID)
                    .append("/")
                    .append(giveaway.getChannelId())
                    .append("/")
                    .append(giveaway.getMessageId())
                    .append("\n");
        }
        return builder.toString();
    }

    public static void showActiveGiveaways(@NotNull SlashCommandInteractionEvent event, List<Giveaway> activeGiveaways) {
        event.reply("Aktywne giveawaye:\n" + getDescriptionOfActiveGiveaways(activeGiveaways))
                .setEphemeral(true)
                .queue();
    }

    public static void endGiveawayAreYouSure(@NotNull SlashCommandInteractionEvent event, int idAsInt) {
        event.reply("Czy jesteś pewien, że chcesz zakończyć giveaway od id=" + idAsInt + " i wylosować nagrody?")
                .setActionRow(
                        Button.success(ComponentId.GIVEAWAY_END_SURE_YES + idAsInt, "Tak"))
                .setEphemeral(true)
                .queue();
    }

    public static void cancelGiveawayAreYouSure(@NotNull SlashCommandInteractionEvent event, int idAsInt) {
        event.reply("Czy jesteś pewien, że chcesz zakończyć giveaway od id=" + idAsInt + " bez losowania nagród?")
                .setActionRow(
                        Button.success(ComponentId.GIVEAWAY_CANCEL_SURE_YES + idAsInt, "Tak"))
                .setEphemeral(true)
                .queue();
    }

    public static void reRollAreYouSure(@NotNull SlashCommandInteractionEvent event, int idAsInt) {
        event.reply("Czy jesteś pewien, że chcesz wylosować na nowo nagrody dla giveaway od id=" + idAsInt + "?")
                .setActionRow(
                        Button.success(ComponentId.GIVEAWAY_RE_ROLL_SURE_YES + idAsInt, "Tak"))
                .setEphemeral(true)
                .queue();
    }

    public static void giveawayEnded(@NotNull ButtonInteractionEvent event) {
        event.reply("Giveaway zakończony").setEphemeral(true).queue();
    }

    public static void giveawayNotPossibleReRoll(@NotNull ButtonInteractionEvent event) {
        event.reply("Giveaway zakończony").setEphemeral(true).queue();
    }

    public static void awaitingConfirmForm(@NotNull ButtonInteractionEvent event) {
        event.reply("**Jeżeli wysłałeś formularz oczekuj na zatwierdzenie przez <@&" + RoleID.DRILL_INSTRUCTOR_ID + ">**")
                .setEphemeral(true)
                .queue(m -> log.info("{} - user waiting to confirm form", event.getUser()));
    }

    public static void cannotAddRankRole(@NotNull SlashCommandInteractionEvent event) {
        event.reply("""
                **Nie można dodać roli stopnia. Możliwe błędy:**
                - Nazwa lub discord ID są puste
                - Nazwa lub discord ID są już w bazie.
                - Discord ID nie jest numerem
                - Rola o podanym discord ID nie istnieje.""").setEphemeral(true).queue();
    }

    public static void writeRankRole(@NotNull SlashCommandInteractionEvent event, @NotNull Rank rank) {
        event.reply("Nazwa: **" + rank.getName() + "**\n" +
                        "Discord ID: **" + rank.getDiscordId().orElse("") + "**\n" +
                        "Skrót: **" + rank.getShortcut() + "**\n")
                .setEphemeral(true)
                .queue();
    }

    public static void rankRoleAdded(@NotNull SlashCommandInteractionEvent event, @NotNull Rank rank) {
        event.reply("Nazwa: **" + rank.getName() + "**\n" +
                        "Discord ID: **" + rank.getDiscordId().orElse("") + "**\n" +
                        "Skrót: **" + rank.getShortcut() + "**\n")
                .setEphemeral(true)
                .queue();
    }
}
