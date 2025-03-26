package pl.mbrzozowski.ranger.configuration.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.exceptions.ContentNotFoundException;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.StringProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.aspectj.util.FileUtil.copyFile;

@Slf4j
@Service
public class ContentService {
    private final String externalPath = "config/content.json";
    private final String internalPath = "src/main/resources/content.json";
    private Map<String, Content> contentMap = new HashMap<>();

    public ContentService() {
        File fileExternal = new File(externalPath);
        if (!fileExternal.exists()) {
            try {
                Files.createDirectories(Paths.get("config"));
                copyFile(new File(internalPath), fileExternal);
                log.info("File copied successfully from internalPath to externalPath.");
            } catch (IOException e) {
                log.error("Error during file copy: " + e.getMessage());
            }
        }
    }

    private void loadContent() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File fileExternal = new File(externalPath);
        File fileInternal = new File(internalPath);

        if (fileExternal.exists()) {
            contentMap = objectMapper.readValue(fileExternal, new TypeReference<>() {
            });
            log.info("External file loaded successfully. - {}", externalPath);
        } else {
            contentMap = objectMapper.readValue(fileInternal, new TypeReference<>() {
            });
            log.info("Internal file loaded successfully. - {}", externalPath);
        }
    }

    /**
     * Retrieves the {@link Content} object associated with the given key.
     *
     * @param key The Key to identifying  requested {@link Content} object.
     * @return The @{@link Content}
     * @throws ContentNotFoundException If the key does not exist in the map.
     */
    private Content getContent(String key) throws ContentNotFoundException, IOException {
        loadContent();
        return Optional.ofNullable(contentMap.get(key)).orElseThrow(() -> new ContentNotFoundException(key));
    }

    /**
     * Retrieves the content associated with a given key from the ContentService. If the user cannot be found
     * or if an error occurs during the retrieval process, appropriate error messages are sent to the user's private channel.
     *
     * @param key    The key used to retrieve the corresponding content from the ContentService.
     * @param userId This ID is used to identify the user and send error messages to their private channel if necessary.
     * @return The content retrieved from the ContentService corresponding to the provided key.
     * @throws RuntimeException If the user cannot be found or the content for the given key cannot be found in the ContentService
     *                          or there is an issue reading the content from the file.
     */
    public Content getContent(String key, String userId) {
        User user = RangersGuild.getUser(userId);
        if (user == null) {
            throw new RuntimeException("User is null");
        }
        try {
            Content content = getContent(key);
            replaceTextToRoleMention(content);
            return content;
        } catch (ContentNotFoundException e) {
            user.openPrivateChannel()
                    .queue(privateChannel
                            -> privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                            "\tERROR - Nie można odnaleźć wartości dla klucza: \"" + key + "\" w pliku \"content.json\"```").queue());
            throw new RuntimeException("Error retrieving content for key: " + key, e);
        } catch (IOException e) {
            user.openPrivateChannel()
                    .queue(privateChannel ->
                            privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                                    "\tERROR - Nie można odczytać pliku \"content.json\"").queue());
            throw new RuntimeException("Can not read a file \"content.json\"", e);
        }
    }

    public String textFormat(@NotNull String message, String userID) {
        if (message.contains("@user")) {
            message = message.replaceAll("@user", User.fromId(userID).getAsMention());
        }
        return message;
    }

    private void replaceTextToRoleMention(@NotNull Content content) {
        content.setMessage(replaceTextToRoleMention(content.getMessage()));
        content.setDescription(replaceTextToRoleMention(content.getDescription()));
        content.setTitle(replaceTextToRoleMention(content.getTitle()));
        for (Field field : content.getFields()) {
            field.setName(replaceTextToRoleMention(field.getName()));
            field.setValue(replaceTextToRoleMention(field.getValue()));
        }
    }

    private String replaceTextToRoleMention(String text) {
        text = replaceTextToRoleMention(text, "@serverAdmin", RoleID.SERVER_ADMIN);
        text = replaceTextToRoleMention(text, "@drill", RoleID.DRILL_INSTRUCTOR_ID);
        return text;
    }

    private String replaceTextToRoleMention(@NotNull String text, String regex, String roleId) {
        if (text.contains(regex)) {
            Role role = RangersGuild.getRoleById(roleId);
            if (role != null) {
                text = text.replaceAll(regex, role.getAsMention());
            }
        }
        return text;
    }
}
