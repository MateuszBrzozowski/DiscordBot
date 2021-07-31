package bot.event.writing;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class Events extends Category{

    private GuildMessageReceivedEvent guildEvent;
    private PrivateMessageReceivedEvent privateEvent;

    public Events(GuildMessageReceivedEvent guildEvent) {
        this.guildEvent = guildEvent;
    }

    public Events(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    void proccessMessage(Message message) {

    }
}
