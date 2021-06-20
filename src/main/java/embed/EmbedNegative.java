package embed;

import helpers.IdRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmbedNegative {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedNegative(GuildMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
            event.getGuild().retrieveMemberById(user.getId()).queue(member -> {
                event.getMessage().delete().submit();
                List<Role> roles = member.getRoles();
                for (int i = 0; i < roles.size(); i++) {
                    if (roles.get(i).getId().equalsIgnoreCase(IdRole.RADA_KLANU)) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(Color.RED);
                        builder.setTitle("WYNIK REKRUTACJI - NEGATYWNY");
                        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
                        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                        event.getChannel().sendMessage(builder.build()).queue();
                        logger.info("Uzytkownik {} wysłał negatywny wynik rekrutacji",member.getNickname());
                        break;
                    }
                }
            });
        });
    }
}
