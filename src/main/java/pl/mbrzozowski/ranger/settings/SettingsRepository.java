package pl.mbrzozowski.ranger.settings;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;


public class SettingsRepository {

    private final String pathFile;
    private final Properties properties = new Properties();

    public SettingsRepository() {
        URL url = getClass().getClassLoader().getResource("settings.properties");
        if (url != null) {
            pathFile = url.getPath();
        } else {
            pathFile = null;
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

    public void save(@NotNull SettingsKey key, String value) {
        loadProperties();
        properties.setProperty(key.getKey(), value);
        storeProperties();
    }

    public void save(@NotNull SettingsKey key, int value) {
        save(key, String.valueOf(value));
    }

    public Optional<String> find(@NotNull SettingsKey key) {
        loadProperties();
        Object o = properties.get(key.getKey());
        if (o == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(properties.get(key.getKey()).toString());
    }
}
