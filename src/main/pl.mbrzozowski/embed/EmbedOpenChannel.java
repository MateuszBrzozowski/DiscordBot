package embed;

import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class EmbedOpenChannel {

    public EmbedOpenChannel(String userID, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Kanał otwarty");
        builder.setDescription("Kanał otwarty przez " + Users.getUserNicknameFromID(userID) + ".");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }
}
