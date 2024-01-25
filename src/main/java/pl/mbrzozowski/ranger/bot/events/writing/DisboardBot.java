package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.disboard.DisboardService;

import java.util.List;

public class DisboardBot extends Proccess {

    private static final String DISBOARD_ID = "302050872383242240";
    private final DisboardService disboardService;

    public DisboardBot(DisboardService disboardService) {
        this.disboardService = disboardService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().getId().equalsIgnoreCase(DISBOARD_ID)) {
            List<MessageEmbed> embeds = event.getMessage().getEmbeds();
            if (embeds.size() == 0) {
                return;
            }
            String description = embeds.get(0).getDescription();
            if (description == null) {
                return;
            }
            description = description.substring(0, 15);
            if (description.equalsIgnoreCase("Podbito serwer!")) {
                disboardService.setDisboardMessage(event.getMessage());
                disboardService.planDeleteMessages();
                disboardService.saveAnswerTime();
                disboardService.setNextReminder();
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}
