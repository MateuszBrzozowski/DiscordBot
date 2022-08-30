package ranger.server.whitelist;

public class Player {

    private final String name;
    private final String id;
    private final String steamID;
    private final int time;

    /**
     * @param name    player name
     * @param id      battlemetrcis id
     * @param steamID steamID player
     * @param time    time in seconds
     */
    public Player(String name, String id, String steamID, int time) {
        this.name = name;
        this.id = id;
        this.steamID = steamID;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getSteamID() {
        return steamID;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", steamID='" + steamID + '\'' +
                ", time=" + time +
                '}';
    }
}
