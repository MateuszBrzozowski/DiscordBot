package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.members.GuildMembersService;

public class AdminCommands extends Proccess {

    private final GuildMembersService guildMembersService;

    public AdminCommands(GuildMembersService guildMembersService) {
        this.guildMembersService = guildMembersService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.DISCORD_MEMBERS)) {
            guildMembersService.getMemberCountCSV(event.getChannel());
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}
