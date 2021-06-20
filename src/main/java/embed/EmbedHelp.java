package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class EmbedHelp {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String RADA_KLANU_ID = "773233884145647666";

    public EmbedHelp(GuildMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getMessage().delete().submit();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            event.getGuild().retrieveMemberById(user.getId()).queue(member -> {
                List<Role> roles = member.getRoles();
                for (int i = 0; i < roles.size(); i++) {
                    if(roles.get(i).getId().equalsIgnoreCase(RADA_KLANU_ID)){
                        user.openPrivateChannel().queue(privateChannel -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.YELLOW);
                            builder.setTitle("Ranger Bot - POMOC");
                            builder.addField("REKRUCI","**!p** - Wysyła na kanale POZYTYWNY wynik rekrutacji\n" +
                                    "**!n** - Wysyła na kanale NEGATYWNY wynik rekrutacji\n" +
                                    "**!close** - Zamyka kanał rekrutacji - rekrut nie widzi kanału/nie może pisać.\n" +
                                    "**!open** - Otwiera kanał rekrutacji - rekrut ponownie może widzieć i pisać na kanale.",false);
                            builder.addField("ZAPISY NA MECZE","**!zapisy <nazwa> <data> <godzina>** - Otwiera nowy kanał, pinguje Clan Member i tworzy listę na mecze \n(przykład: !zapisy CCFN 19.06.2021 19:30)",false);
                            builder.setFooter("RangerBot created by © Brzozaaa");
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            privateChannel.sendMessage(builder.build()).queue();
                            logger.info("Uzytkownik: {} poprosił o pomoc. Wiadomosc prywatna z pomocą wysłana.", member.getNickname());
                        });
                    }
                }
            });

        });

    }
}
