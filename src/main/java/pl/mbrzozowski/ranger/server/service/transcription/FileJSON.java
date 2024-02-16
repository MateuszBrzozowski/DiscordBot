package pl.mbrzozowski.ranger.server.service.transcription;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pl.mbrzozowski.ranger.exceptions.FileNotFoundException;

import java.io.*;
import java.time.LocalDateTime;

@Slf4j
public class FileJSON {

    private final static String PATH = "ticket-transcripts\\";
    private final boolean isDir = new File(PATH).mkdirs();
    private final File file;

    public FileJSON(String name) {
        this.file = new File(PATH + name + ".json");
    }

    public void openTicket(String userName) {
        writeMessage(userName, convertDateToString(LocalDateTime.now()), "Klient");
    }

    private void checkFile(@NotNull File file) {
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

    @NotNull
    private String convertDateToString(@NotNull LocalDateTime date) {
        return String.format("%02d", date.getDayOfMonth()) +
                "/" +
                String.format("%02d", date.getMonthValue()) +
                "/" +
                date.getYear() +
                " " +
                String.format("%02d", date.getHour()) +
                ":" +
                String.format("%02d", date.getMinute());
    }

    public void writeMessage(String name, LocalDateTime date, String message) {
        String dateAsString = convertDateToString(date);
        writeMessage(name, dateAsString, message);
    }

    public void writeMessage(String userName, String date, String message) {
        checkFile(this.file);
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
        checkFile(file);
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

    @Nullable
    public File convertMessagesToHTML() {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        File htmlFile = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                JSONObject object = new JSONObject(line);
                String name = object.get("name").toString();
                String date = object.get("date").toString();
                String message = object.get("message").toString();
                i++;
                if (i == 1) {
                    htmlFile = createHTMLFile(name, date);
                    continue;
                }
                writeMessageBox(htmlFile, name, date, message);
            }
            if (i <= 1) {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeEndHTMLFile(htmlFile);
        clear();
        return htmlFile;
    }

    private void writeMessageBox(File file, String name, String date, String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.append("<div class=\"message-box\">");
            writer.append("<div class=\"header\">");
            writer.append(" <div class=\"user-name\">");
            writer.append(name);
            writer.append("</div>");
            writer.append("<div class=\"date\">");
            writer.append(date);
            writer.append("</div></div>");
            writer.append("<div class=\"message\">");
            writer.append(message);
            writer.append("</div></div>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @NotNull
    private File createHTMLFile(String name, @NotNull String date) {
        File file = new File(PATH + name + "_" +
                date.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "_") +
                ".html");
        checkFile(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writeHead(writer, name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private void writeHead(BufferedWriter writer, String name) throws java.io.FileNotFoundException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ticketHeader.html");
        if (inputStream == null) {
            throw new java.io.FileNotFoundException();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.append(line.replaceAll(" ", "").replaceAll("\n", "")
                        .replace("<title>UserName</title>", "<title>" + name + "</title>"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEndHTMLFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.append("</body></html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        if (file.exists()) {
            this.file.delete();
        }
    }
}
