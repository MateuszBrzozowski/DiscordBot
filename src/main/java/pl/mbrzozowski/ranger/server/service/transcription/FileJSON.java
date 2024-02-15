package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;

@Slf4j
public class FileJSON {

    private final static String PATH = "ticket-transcripts\\";
    private final boolean isDir = new File(PATH).mkdirs();
    private final File file;

    public FileJSON(String name) {
        this.file = new File(PATH + name + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                log.info("File created - {}", file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("File exist - {}", file.getAbsolutePath());
        }
    }

    public void writeMessage(String userName, String date, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", userName);
        jsonObject.put("date", date);
        jsonObject.put("message", message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.append(jsonObject.toString()).append("\n");
            log.info("Message saved");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeAmountAttachments(int amount) {
        if (file.exists()) {
            try {
                file.delete();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", amount);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.append(jsonObject.toString()).append("\n");
            log.info("Amount of attachments saved");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readAmountAttachments() {
        String content = getResponseContent();
        if (StringUtils.isBlank(content)) {
            return 0;
        }
        JSONObject jsonObject = new JSONObject(content);
        Object amount = jsonObject.get("amount");
        return amount instanceof Integer ? ((Integer) amount) : 0;
    }

    @NotNull
    private String getResponseContent() {
        StringBuilder responseContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return responseContent.toString();
    }
}
