package embed;

import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ranger.Repository;
import recrut.Recruits;

import java.awt.*;

public class EmbedNegative {

    public EmbedNegative(GuildMessageReceivedEvent event) {
        Recruits recruits = Repository.getRecruits();
        if (recruits.isRecruitChannel(event.getChannel().getId())) {
            event.getChannel().sendMessage("<@" + recruits.getRecruitIDFromChannelID(event.getChannel().getId()) + ">").queue();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.RED);
            builder.setTitle("WYNIK REKRUTACJI - NEGATYWNY");
            builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setFooter("Podpis: " + Users.getUserNicknameFromID(event.getAuthor().getId()));
            event.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
