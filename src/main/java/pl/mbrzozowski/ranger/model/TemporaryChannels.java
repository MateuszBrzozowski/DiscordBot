package pl.mbrzozowski.ranger.model;

public interface TemporaryChannels {

    void deleteChannelById(String channelId);
    void deleteFromDBByChannelId(String channelId);
}
