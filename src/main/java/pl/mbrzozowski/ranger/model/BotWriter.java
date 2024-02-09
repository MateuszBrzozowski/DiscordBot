package pl.mbrzozowski.ranger.model;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.guild.RangersGuild;

@Service
public class BotWriter {

    private boolean isActive = false;
    private String channelID;

    public void sendMsg(String msg) {
        isActive = false;
        Guild guild = RangersGuild.getGuild();
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
