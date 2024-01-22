package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;

public class ClanMemberCheck extends Proccess {

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        boolean isClanMember = Users.hasUserRole(event.getAuthor().getId(), RoleID.CLAN_MEMBER_ID);
        if (isClanMember) {
            getNextProccess().proccessMessage(event);
        }
    }
}
