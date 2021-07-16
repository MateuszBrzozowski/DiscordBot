package embed;

import recrut.Recruits;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;

public class EmbedPositive {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedPositive(GuildMessageReceivedEvent event) {
        Recruits recruits = RangerBot.getRecruits();
        if (recruits.isRecruitChannel(event.getChannel().getId())) {
            EmbedBuilder builder = new EmbedBuilder();
            event.getChannel().sendMessage("Gratulacje <@" + recruits.getRecruitIDFromChannelID(event) + ">").queue();
            builder.setColor(Color.GREEN);
            builder.setTitle("WYNIK REKRUTACJI - POZYTYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
            event.getChannel().sendMessage(builder.build()).queue();
            logger.info("Uzytkownik {} wysłał pozytywny wynik rekrutacji", event.getMember().getNickname());
        }
    }
}
