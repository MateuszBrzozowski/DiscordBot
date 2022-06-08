package server.whitelist;


import helpers.RangerLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Battlemetrics {

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        LocalDateTime dateTimeNow = LocalDateTime.now();
        LocalDateTime dateTimeMinusMonth = dateTimeNow.minusDays(30);
        int offset = 0;
        boolean getNextPage = true;
        OkHttpClient client = new OkHttpClient();
        while (getNextPage) {
            try {
                Request request = new Request.Builder()
                        .url("https://api.battlemetrics.com/servers/3508670/relationships/leaderboards/time?filter[period]=" + dateTimeMinusMonth + ":" + dateTimeNow + "&page[offset]=" + offset + "&page[size]=100")
                        .addHeader("Authorization", Auth.BM_AUTH_VALUE)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    JSONObject leaderboardPlayer = new JSONObject(response.body().string());
                    JSONArray data = leaderboardPlayer.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject player = data.getJSONObject(i);
                        JSONObject playerAttributes = (JSONObject) player.get("attributes");
                        String id = player.get("id").toString();
                        String name = playerAttributes.get("name").toString();
                        int time = Integer.parseInt(playerAttributes.get("value").toString());
                        String steamID = getSteamID(id);
                        if (time < Whitelist.MIN_SECONDS) {
                            getNextPage = false;
                            break;
                        }
                        if (steamID != null) {
                            Player p = new Player(name, id, steamID, time);
                            players.add(p);
                        }
                    }
                } else {
                    RangerLogger.info("BATTLEMETRICS ERROR: " + response.code());
                    System.out.println("BATTLEMETRICS ERROR: " + response.code());
                    System.out.println(response.request().url());
                    return null;
                }
            } catch (IOException e) {
                RangerLogger.info("Błąd pobierania graczy z battlemetrics: " + e.getMessage());
            }
            offset += 100;
        }

        return players;
    }


    private String getSteamID(String id) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request requestSteamID = new Request.Builder()
                .url("https://api.battlemetrics.com/players/" + id + "?include=identifier&fields[identifier]=type%2Cidentifier")
                .addHeader("Authorization", Auth.BM_AUTH_VALUE)
                .build();
        Response responseSteamID = client.newCall(requestSteamID).execute();
        if (responseSteamID.code() == 200) {
            String bodySteamID = responseSteamID.body().string();
            JSONObject user = new JSONObject(bodySteamID);
            JSONArray userIncluded = user.getJSONArray("included");
            for (int i = 0; i < userIncluded.length(); i++) {
                JSONObject attributes = (JSONObject) userIncluded.getJSONObject(i).get("attributes");
                String type = attributes.get("type").toString();
                if (type.equalsIgnoreCase("steamID")) {
                    return attributes.get("identifier").toString();
                }
            }
        }
        return null;
    }
}
