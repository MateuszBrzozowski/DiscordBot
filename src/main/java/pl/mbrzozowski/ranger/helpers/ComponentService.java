package pl.mbrzozowski.ranger.helpers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mbrzozowski.ranger.Repository;
import pl.mbrzozowski.ranger.embed.EmbedInfo;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.server.service.ServerService;

import java.util.ArrayList;
import java.util.List;

public class ComponentService {

    private final RecruitsService recruitsService;
    private final ServerService serverService;

    @Autowired
    public ComponentService(RecruitsService recruitsService, ServerService serverService) {
        this.recruitsService = recruitsService;
        this.serverService = serverService;
    }

    public static void disableButtons(String channelID, String messageID) {
        TextChannel textChannel = Repository.getJda().getTextChannelById(channelID);
        if (textChannel != null) {
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
            Guild guild = event.getGuild();
            if (guild != null) {
                TextChannel textChannel = guild.getTextChannelById(event.getChannel().getId());
                if (textChannel != null) {
                    textChannel.delete().queue();
                }
            }
        });
        whichCategory(event);
        thread.start();
    }

    private void whichCategory(@NotNull ButtonInteractionEvent event) {
        String parentCategoryId = event.getTextChannel().getParentCategoryId();
        if (parentCategoryId != null) {
            if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                recruitsService.deleteChannelByID(event.getChannel().getId());
            } else if (parentCategoryId.equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER)) {
                serverService.removeChannel(event);
            }
        }
    }
}
