package pl.mbrzozowski.ranger.model;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;

@Service
public class BotWriter {

    private boolean isActive = false;
    private String channelID;

    public void sendMsg(String msg) {
        isActive = false;
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(channelID);
            if (textChannel != null) {
                textChannel.sendMessage(msg).queue();
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
        isActive = true;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
