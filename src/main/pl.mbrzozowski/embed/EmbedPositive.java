package embed;

import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ranger.Repository;
import recrut.Recruits;

import java.awt.*;

public class EmbedPositive {

    public EmbedPositive(GuildMessageReceivedEvent event) {
        Recruits recruits = Repository.getRecruits();
        if (recruits.isRecruitChannel(event.getChannel().getId())) {
            EmbedBuilder builder = new EmbedBuilder();
            event.getChannel().sendMessage("Gratulacje <@" + recruits.getRecruitIDFromChannelID(event.getChannel().getId()) + ">").queue();
            builder.setColor(Color.GREEN);
            builder.setTitle("WYNIK REKRUTACJI - POZYTYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(event.getAuthor().getId()));
            event.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
