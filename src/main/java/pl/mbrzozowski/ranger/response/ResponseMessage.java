package pl.mbrzozowski.ranger.response;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import pl.mbrzozowski.ranger.helpers.Users;

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
        event.reply("**REKRTUACJA DO KLANU RANGERS POLSKA TYMCZASOWO ZAMKNIĘTA!**")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkonik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Maksymalna liczba kanałów w kategorii StrefaRekruta.");
    }

    public static void userBlackList(@NotNull ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkonik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Jest na czarnej liście.");
    }
}
