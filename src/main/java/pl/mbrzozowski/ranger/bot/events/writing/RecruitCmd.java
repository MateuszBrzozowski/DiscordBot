package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.configuration.content.ContentService;
import pl.mbrzozowski.ranger.guild.Commands;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.response.EmbedInfo;

@Slf4j
public class RecruitCmd extends Proccess {

    private final RecruitsService recruitsService;
    private final ContentService contentService;

    public RecruitCmd(RecruitsService recruitsService, ContentService contentService) {
        this.recruitsService = recruitsService;
        this.contentService = contentService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.START_REKRUT)) {
                event.getMessage().delete().submit();
                EmbedInfo.recruiter(event, contentService);
                log.info("{} - msg({}) - creates embed for recruit application", event.getAuthor(), event.getMessage().getContentRaw());
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.NEGATIVE)) {
                event.getMessage().delete().submit();
                recruitsService.negativeResult(event.getAuthor().getId(), event.getChannel().asTextChannel());
                log.info(event.getAuthor() + " - Send negative result for recruit");
            } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.POSITIVE)) {
                event.getMessage().delete().submit();
                recruitsService.positiveResult(event.getAuthor().getId(), event.getChannel().asTextChannel());
                log.info(event.getAuthor() + " - Send positive result for recruit");
            } else {
                getNextProccess().proccessMessage(event);
            }
        } else {
            getNextProccess().proccessMessage(event);
        }
    }
}
