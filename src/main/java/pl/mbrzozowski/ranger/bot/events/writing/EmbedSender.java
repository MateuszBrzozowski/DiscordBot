package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.role.RoleService;

@Slf4j
public class EmbedSender extends Proccess {

    private final RoleService roleService;

    public EmbedSender(MessageReceivedEvent messageReceivedEvent, RoleService roleService) {
        super(messageReceivedEvent);
        this.roleService = roleService;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(messageReceived.getChannel().asTextChannel());
            log.info("{} - msg({}) - creates embed to seeders role", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.ROLES)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.sendRoles(messageReceived, roleService);
            log.info("{} - msg({}) - creates embed for discord roles", messageReceived.getAuthor(), message.getContentDisplay());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.RECRUIT_OPINIONS)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.recruitOpinionsFormOpening(messageReceived);
            log.info("{} - msg({}) - creates embed for recruit opinion", messageReceived.getAuthor(), message.getContentDisplay());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
