package pl.mbrzozowski.ranger.server.seed.call;

import pl.mbrzozowski.ranger.settings.SettingsService;

public abstract class Factory {
    public abstract MessageCall getLevelOfMessageCall(MessageService messageService, Levels level, SettingsService settingsService);
}
