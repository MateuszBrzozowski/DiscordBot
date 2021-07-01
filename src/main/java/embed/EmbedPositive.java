package embed;

import helpers.IdRole;
import model.Recruits;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.entities.UserById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;
import java.util.List;

public class EmbedPositive {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedPositive(GuildMessageReceivedEvent event) {
        Recruits recruits = RangerBot.getRecruits();
        if (recruits.isRecruitChannel(event.getChannel().getId())){
            event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
                event.getGuild().retrieveMemberById(user.getId()).queue(member -> {
                    event.getMessage().delete().submit();
                    List<Role> roles = member.getRoles();
                    for (int i = 0; i < roles.size(); i++) {
                        if (roles.get(i).getId().equalsIgnoreCase(IdRole.RADA_KLANU)) {
                            EmbedBuilder builder = new EmbedBuilder();
                            event.getChannel().sendMessage("Gratulacje <@"+ recruits.getRecruitIDFromChannelID(event) +">").queue();
                            builder.setColor(Color.GREEN);
                            builder.setTitle("WYNIK REKRUTACJI - POZYTYWNY");
                            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            event.getChannel().sendMessage(builder.build()).queue();
                            logger.info("Uzytkownik {} wysłał pozytywny wynik rekrutacji",member.getNickname());
                        }
                    }
                });
            });
        }
    }
}
