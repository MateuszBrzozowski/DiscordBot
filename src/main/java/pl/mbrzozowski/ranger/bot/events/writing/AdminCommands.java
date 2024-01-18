package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.members.GuildMembersService;

public class AdminCommands extends Proccess {

    private final GuildMembersService guildMembersService;

    public AdminCommands(MessageReceivedEvent messageReceived, GuildMembersService guildMembersService) {
        super(messageReceived);
        this.guildMembersService = guildMembersService;
    }

    @Override
    public void proccessMessage(@NotNull Message message) {
        if (message.getContentDisplay().equalsIgnoreCase(Commands.DISCORD_MEMBERS)) {
            guildMembersService.getMemberCountCSV(messageReceived.getChannel());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
