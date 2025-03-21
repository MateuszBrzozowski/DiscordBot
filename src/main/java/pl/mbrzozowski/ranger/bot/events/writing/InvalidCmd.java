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
        String version = InvalidCmd.class.getPackage().getImplementationVersion();
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(
                            "Niestety, nie rozumiem Ciebie.\n" +
                                    "**!event** - otwiera generator eventów.\n" +
                                    "**!eventsEdit** - edytowanie aktywnych eventów.\n" +
                                    "App version: " + version)
                    .queue(message1 -> log.info("{}, {}, {} - misunderstood message", privateChannel, privateChannel.getUser(), event.getMessage().getContentRaw())));
            return;
        }
        log.error("{}, {}, {} - misunderstood message or user is null", event.getChannel(), event.getAuthor(), event.getMessage().getContentRaw());
    }
}
