package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.members.GuildMembersService;
import pl.mbrzozowski.ranger.members.clan.hours.HoursService;
import pl.mbrzozowski.ranger.members.clan.rank.RankService;

@RequiredArgsConstructor
public class AdminCommands extends Proccess {

    private final GuildMembersService guildMembersService;
    private final HoursService hoursService;
    private final RankService rankService;

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.DISCORD_MEMBERS)) {
            guildMembersService.getMemberCountCSV(event.getChannel());
        } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.STEAM_HOURS)) {
            hoursService.getUserHoursAndExportToFile(event);
        } else if (event.getMessage().getAttachments().size() == 1) {
            rankService.update(event);
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}
