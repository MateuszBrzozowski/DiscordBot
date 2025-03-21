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
    public Content getContent(String key) throws ContentNotFoundException, IOException {
        loadContent();
        return Optional.ofNullable(contentMap.get(key)).orElseThrow(() -> new ContentNotFoundException(key));
    }

    public String textFormat(@NotNull String message, String userID) {
        if (message.contains("@user")) {
            message = message.replaceAll("@user", User.fromId(userID).getAsMention());
        }
        if (message.contains("@serverAdmin")) {
            Role role = RangersGuild.getRoleById(RoleID.SERVER_ADMIN);
            if (role != null) {
                message = message.replaceAll("@serverAdmin", role.getAsMention());
            }
        }
        return message;
    }
}
