package pl.mbrzozowski.ranger.members.clan.hours;

import lombok.Data;

@Data
public class SteamUser {

    private final String name;
    private final String steamId;
    private int playtimeForever; // minutes

    public SteamUser(String name, String steamId) {
        this.name = name;
        this.steamId = steamId;
    }

    public int getHours() {
        return playtimeForever / 60;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", steamId='" + steamId + '\'' +
                ", playtimeForever='" + getHours() + "h'" +
                '}';
    }
}
