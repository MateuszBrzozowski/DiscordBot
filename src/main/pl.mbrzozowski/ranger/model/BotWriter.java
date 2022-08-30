package ranger.model;

import ranger.helpers.CategoryAndChannelID;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import ranger.Repository;

public class BotWriter {

    private boolean isActive = false;
    private String channelID;

    public void sendMsg(String msg) {
        isActive = false;
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID).getTextChannelById(channelID);
        textChannel.sendMessage(msg).queue();
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
