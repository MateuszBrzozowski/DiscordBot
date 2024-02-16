package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
public class FileAttachments {

    private static final Set<String> EXTENSIONS = new HashSet<>(Arrays.asList("jpg",
            "jpeg", "png", "gif", "webp", "tiff", "svg", "apng", "webm", "flv", "vob",
            "avi", "mov", "wmv", "amv", "mp4", "mpg", "mpeg", "gifv"));
    private final static String PATH = "ticket-transcripts\\";
    private final boolean isDir = new File(PATH).mkdirs();
    private Message.Attachment attachment;
    private final String channelId;
    private final List<File> files = new ArrayList<>();
    private FileJSON fileJSON;

    public FileAttachments(Message.Attachment attachment, String channelId) {
        this.attachment = attachment;
        this.channelId = channelId;
        log.info("New attachment for channel {}", channelId);
    }

    public FileAttachments(String channelId) {
        this.channelId = channelId;
    }

    public void saveFile() {
        if (isNotSupportedExtension(attachment.getFileExtension())) {
            log.warn("{} - extension not supported", attachment.getFileExtension());
            return;
        }
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

    private boolean isNotSupportedExtension(String fileExtension) {
        for (String extension : EXTENSIONS) {
            if (extension.equals(fileExtension)) {
                return false;
            }
        }
        return true;
    }

    public Collection<FileUpload> getAttachments() {
        fileJSON = new FileJSON(channelId + "_attachments");
        int attachmentsSize = fileJSON.readAmountAttachments();
        if (attachmentsSize == 0) {
            return new ArrayList<>();
        }
        ArrayList<FileUpload> fileUploads = new ArrayList<>();
        for (int i = 1; i <= attachmentsSize; i++) {
            File file = null;
            for (String extension : EXTENSIONS) {
                file = new File(PATH + channelId + "_" + i + "." + extension);
                if (file.exists()) {
                    break;
                }
            }
            if (file != null && file.exists()) {
                fileUploads.add(FileUpload.fromData(file));
                files.add(file);
            }
        }
        return fileUploads;
    }

    public void clear() {
        for (File file : files) {
            file.delete();
        }
        if (fileJSON != null) {
            fileJSON.clear();
        }
    }
}
