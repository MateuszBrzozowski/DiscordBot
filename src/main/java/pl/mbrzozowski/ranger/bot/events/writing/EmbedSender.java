package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.role.RoleService;

public class EmbedSender extends Proccess {

    private final RoleService roleService;

    public EmbedSender(MessageReceivedEvent messageReceivedEvent, RoleService roleService) {
        super(messageReceivedEvent);
        this.roleService = roleService;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.SQUAD_SEEDERS_INFO)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.seedersRoleJoining(messageReceived.getChannel().asTextChannel());
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.ROLES)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.sendRoles(messageReceived, roleService);
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.RECRUIT_OPINIONS)) {
            messageReceived.getMessage().delete().submit();
            EmbedInfo.recruitOpinionsFormOpening(messageReceived);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
