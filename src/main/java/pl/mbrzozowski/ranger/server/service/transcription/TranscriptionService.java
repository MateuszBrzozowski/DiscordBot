package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.server.service.Client;
import pl.mbrzozowski.ranger.server.service.ServerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private final ServerService serverService;

    public boolean isTicket(@NotNull MessageReceivedEvent event) {
        Optional<Client> optional = serverService.findByChannelId(event.getChannel().getId());
        return optional.isPresent();
    }

    public void saveMessage(@NotNull MessageReceivedEvent event) {
        log.info("Message on server ticket.");
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.size() > 0) {
            for (Message.Attachment attachment : attachments) {
                FileAttachments fileAttachments = new FileAttachments(attachment, event.getChannel().getId());
                fileAttachments.saveFile();
            }
        }
        if (StringUtils.isBlank(event.getMessage().getContentRaw())) {
            log.info("No content raw in message");
            return;
        }
        String name = Objects.requireNonNull(event.getMessage().getMember()).getNickname();
        LocalDateTime date = event.getMessage().getTimeCreated().toLocalDateTime();
        String message = event.getMessage().getContentRaw();
        if (attachments.size() > 0) {
            message+= " + (załącznik)";
        }
        String dateAsString = convertDateToString(date);
        FileJSON fileJSON = new FileJSON(event.getChannel().getId());
        fileJSON.writeMessage(name, dateAsString, message);
    }

    @NotNull
    private String convertDateToString(@NotNull LocalDateTime date) {
        return date.getDayOfMonth() +
                "." +
                String.format("%02d", date.getMonthValue()) +
                "." +
                date.getYear();
    }
}
