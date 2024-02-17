package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.guild.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class EmbedSender extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            event.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(event.getChannel().asTextChannel());
            log.info("{} - msg({}) - creates embed to seeders role", event.getAuthor(), event.getMessage().getContentRaw());
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}
