package embed;

import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class EmbedCloseChannel {

    public EmbedCloseChannel(String userID, TextChannel channel) {
        String userName = Users.getUserNicknameFromID(userID);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Kanał zamknięty");
        builder.setDescription("Kanał zamknięty przez " + userName + ".");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessage(builder.build()).queue();
    }
}
