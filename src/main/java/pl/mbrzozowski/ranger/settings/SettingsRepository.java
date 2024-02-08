package pl.mbrzozowski.ranger.settings;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Repository
public class SettingsRepository {

    private final String pathFile = "settings.properties";
    private final Properties properties = new Properties();

    public SettingsRepository() {
        File file = new File(pathFile);
        if (!file.exists()) {
            log.info("File {} not exist", pathFile);
            try {
                file.createNewFile();
                log.info("Created file {}", pathFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadProperties() {
        try (FileInputStream fileInputStream = new FileInputStream(pathFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeProperties() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(pathFile)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void save(String key, String value) {
        loadProperties();
        properties.setProperty(key, value);
        log.debug("Saved settings({}={})", key, value);
        storeProperties();
    }

    Optional<String> find(@NotNull SettingsKey key) {
        loadProperties();
        Object o = properties.get(key.getKey());
        if (o == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(properties.get(key.getKey()).toString());
    }

    Optional<String> find(@NotNull String key) {
        loadProperties();
        Object o = properties.get(key);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(properties.get(key).toString());
    }

    public void deleteByKey(String key) {
        loadProperties();
        properties.remove(key);
        storeProperties();
    }
}
