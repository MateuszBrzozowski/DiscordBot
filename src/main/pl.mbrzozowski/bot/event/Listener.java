package bot.event;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class Listener extends ListenerAdapter {
    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
//        EmbedInfo.sendHelloMessagePrivate(event.getUser().getId());
//        JDA jda = Repository.getJda();
//        jda.getTextChannelById(CategoryAndChannelID.CHANNEL_TESTUJEMY).sendMessage("Hey <@" + event.getUser().getId() + ">, welcome to **Rangers Polska!**");
    }
}
