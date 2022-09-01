package ranger.helpers;

import ranger.embed.EmbedInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;
import ranger.recruit.RecruitsService;

import java.util.ArrayList;
import java.util.List;

public class ComponentService {

    private final RecruitsService recruitsService;

    public ComponentService(RecruitsService recruitsService) {
        this.recruitsService = recruitsService;
    }

    public static void disableButtons(String channelID, String messageID) {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelID);
        textChannel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            List<Button> buttons = message.getButtons();
            List<Button> buttonsNew = new ArrayList<>();
            for (Button b : buttons) {
                b = b.asDisabled();
                buttonsNew.add(b);
            }
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessageEmbeds(messageEmbed).setActionRow(buttonsNew).queue();
        });
    }

    public void removeChannel(@NotNull ButtonInteractionEvent event) {
        disableButtons(event.getChannel().getId(), event.getMessageId());
        EmbedInfo.removedChannel(event.getTextChannel());
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().queue();
        });
        whichCategory(event);
        thread.start();
    }

    private void whichCategory(ButtonInteractionEvent event) {
        String parentCategoryId = event.getTextChannel().getParentCategoryId();
        if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
            recruitsService.deleteChannelByID(event.getChannel().getId());
        } else if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER)) {
            Repository.getServerService().removeChannel(event);
        }
    }
}
