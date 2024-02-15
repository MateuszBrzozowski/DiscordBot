package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.util.concurrent.ExecutionException;

@Slf4j
public class FileAttachments {

    private final static String PATH = "ticket-transcripts\\";
    private final boolean isDir = new File(PATH).mkdirs();
    private final Message.Attachment attachment;
    private final String channelId;

    public FileAttachments(Message.Attachment attachment, String channelId) {
        this.attachment = attachment;
        this.channelId = channelId;
        log.info("New attachment for channel {}", channelId);
    }

    public void saveFile() {
        FileJSON fileJSON = new FileJSON(channelId + "_attachments");
        int amount = fileJSON.readAmountAttachments();
        amount++;
        try {
            attachment.getProxy().downloadToFile(new File(PATH + channelId + "_" + amount + "." + attachment.getFileExtension())).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        fileJSON.writeAmountAttachments(amount);
    }
}
