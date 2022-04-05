package recrut;

import embed.EmbedInfo;
import embed.EmbedSettings;
import model.CleanerChannel;
import model.MemberWithPrivateChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.time.OffsetDateTime;
import java.util.List;

public class CleanerRecruitChannel implements CleanerChannel {

    protected static final Logger logger = LoggerFactory.getLogger(EmbedInfo.class.getName());
    private final List<MemberWithPrivateChannel> activeRecruits;
    private final int DELAY_IN_DAYS = 7;

    public CleanerRecruitChannel(List<MemberWithPrivateChannel> activeRecruits) {
        this.activeRecruits = activeRecruits;
    }

    public void clean() {
        if (!activeRecruits.isEmpty()) {
            JDA jda = Repository.getJda();
            for (int i = 0; i < activeRecruits.size(); i++) {
                String channelID = activeRecruits.get(i).getChannelID();
                jda.getTextChannelById(channelID).getHistory().retrievePast(100).queue(messages -> {
                    if (isTimeToRemove(messages)) {
                        Recruits recruits = Repository.getRecruits();
                        recruits.deleteChannel(messages.get(0).getChannel().getId());
                    }
                });
            }
        }
    }

    private boolean isTimeToRemove(List<Message> messages) {
        int indexMessage = searchResultMessage(messages);
        if (indexMessage != -1) {
            OffsetDateTime timeCreated = messages.get(indexMessage).getTimeCreated();
            OffsetDateTime timeNow = OffsetDateTime.now();
            timeCreated = timeCreated.plusDays(DELAY_IN_DAYS);
            if (timeCreated.isBefore(timeNow)) {
                return true;
            }
        }
        return false;
    }

    private int searchResultMessage(List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (checkEmbeds(embeds)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param embeds -sprawdzane embeds
     * @return true jeśli wiadomść zawiera wynik rekrutacji, w innym przyadku false
     */
    private boolean checkEmbeds(List<MessageEmbed> embeds) {
        if (!embeds.isEmpty()) {
            return isEmbedTitle(embeds.get(0));
        }
        return false;
    }

    /**
     * @param embed sprawdzany embed
     * @return true jeśli Embed to wynik rekrutacji, w innym przypadku false
     */
    private boolean isEmbedTitle(MessageEmbed embed) {
        String title = embed.getTitle();
        if (title != null && title.length() >= EmbedSettings.RESULT.length()) {
            title = title.substring(0, EmbedSettings.RESULT.length());
            if (title.equalsIgnoreCase(EmbedSettings.RESULT)) {
                return true;
            }
        }
        return false;
    }
}
