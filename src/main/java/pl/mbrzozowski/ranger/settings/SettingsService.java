package pl.mbrzozowski.ranger.settings;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public void save(SettingsKey key, String value) {
        settingsRepository.save(key, value);
    }

    public void save(@NotNull SettingsKey key, int value) {
        save(key, String.valueOf(value));
    }

    public void save(String key, int value) {
        settingsRepository.save(key, String.valueOf(value));
    }

    public Optional<String> find(@NotNull SettingsKey key) {
        return settingsRepository.find(key);
    }

    public Optional<String> find(String key) {
        return settingsRepository.find(key);
    }
}
