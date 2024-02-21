package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.Users;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptionService {

    public void openTicket(String channelId, String userName) {
        FileJSON fileJSON = new FileJSON(channelId);
        userName = Users.replaceAllIllegalCharsInName(userName);
        fileJSON.openTicket(userName);
    }

    public void saveMessage(@NotNull MessageReceivedEvent event) {
        log.info("Message on server ticket.");
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        int attachmentsNumber = 1;
        if (attachments.size() > 0) {
            for (Message.Attachment attachment : attachments) {
                FileAttachments fileAttachments = new FileAttachments(attachment, event.getChannel().getId());
                attachmentsNumber = fileAttachments.saveFile();
            }
        }
        String name = Users.getNickname(Objects.requireNonNull(event.getMember()));
        LocalDateTime date = event.getMessage().getTimeCreated().toLocalDateTime();
        String message = event.getMessage().getContentRaw();
        if (attachments.size() > 0) {
            message += " + (załącznik numer [" + attachmentsNumber + "])";
        }
        FileJSON fileJSON = new FileJSON(event.getChannel().getId());
        fileJSON.writeMessage(name, date, message);
    }

    public void createAndSendTranscript(String channelID) {
        FileJSON fileJSON = new FileJSON(channelID);
        File transcript = fileJSON.convertMessagesToHTML();
        if (transcript == null) {
            log.warn("Transcript file null");
            return;
        }
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.ARCHIVES_TICKET);
        if (textChannel == null) {
            log.warn("Null text channel");
            return;
        }
        FileAttachments fileAttachments = new FileAttachments(channelID);
        List<File> files = fileAttachments.getAttachments();
        File zipFile = zipFiles(transcript, files);
        textChannel.sendMessage(transcript.getName())
                .addFiles(FileUpload.fromData(zipFile))
                .queue(message -> {
                    transcript.delete();
                    fileAttachments.clear();
                    zipFile.delete();
                });
    }

    private File zipFiles(@NotNull File transcript, @NotNull List<File> files) {
        Path path = Paths.get(transcript.getAbsolutePath()).getParent().toAbsolutePath();
        try (ZipFile zipFile = new ZipFile(path + "/" + transcript.getName() + ".zip")) {
            files.add(transcript);
            zipFile.addFiles(files);
            return zipFile.getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
