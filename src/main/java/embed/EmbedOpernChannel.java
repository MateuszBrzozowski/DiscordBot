package embed;

import helpers.RoleID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class EmbedOpernChannel {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedOpernChannel(GuildMessageReceivedEvent event) {
        event.getGuild().retrieveMemberById(event.getMessage().getAuthor().getId()).queue(member -> {
            List<Role> roles = member.getRoles();
            for (int i = 0; i < roles.size(); i++) {
                if (roles.get(i).getId().equalsIgnoreCase(RoleID.RADA_KLANU)){
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.GREEN);
                    builder.setTitle("Kanał otwarty");
                    builder.setDescription("Kanał otwarty przez " + event.getAuthor().getName() + ".");
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    event.getChannel().sendMessage(builder.build()).queue();
                    logger.info("Uzytkownik {} otworzył kanał", member.getNickname());
                    break;
                }
            }
        });
    }
}
