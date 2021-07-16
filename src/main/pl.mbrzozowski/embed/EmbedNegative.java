package embed;

import recrut.Recruits;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;

public class EmbedNegative {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedNegative(GuildMessageReceivedEvent event) {
        Recruits recruits = RangerBot.getRecruits();
        if (recruits.isRecruitChannel(event.getChannel().getId())) {
            event.getChannel().sendMessage("<@" + recruits.getRecruitIDFromChannelID(event) + ">").queue();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("WYNIK REKRUTACJI - NEGATYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            event.getChannel().sendMessage(builder.build()).queue();
            logger.info("Uzytkownik {} wysłał negatywny wynik rekrutacji", event.getMember().getNickname());
        }

    }
}
