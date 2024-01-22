package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;

@Slf4j
public class InvalidCmd extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        User user = DiscordBot.getJda().getUserById(event.getAuthor().getId());
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("""
                            Niestety, nie rozumiem Ciebie.\s
                            **!event** - otwiera generator eventów.
                            **!eventsEdit** - edytowanie aktywnych eventów.
                            Jeżeli potrzebujesz więcej pomocy. Wpisz **!help**""")
                    .queue(message1 -> log.info("{} - misunderstood message", event.getAuthor())));
            return;
        }
        log.error("{} - misunderstood message or user is null", event.getAuthor());
    }
}
