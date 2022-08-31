package ranger.response;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

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
}