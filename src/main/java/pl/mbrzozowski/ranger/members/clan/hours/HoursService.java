package pl.mbrzozowski.ranger.members.clan.hours;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.members.clan.ClanMember;
import pl.mbrzozowski.ranger.members.clan.ClanMemberService;
import pl.mbrzozowski.ranger.model.TempFiles;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HoursService {

    private static final int APP_ID_SQUAD = 393380;
    private final List<SteamUser> users = new ArrayList<>();
    private final ClanMemberService clanMemberService;
    private final String API_KEY;
    private HttpURLConnection connection;
    private TempFiles resultCSV;
    private TempFiles logFiles;


    public HoursService(ClanMemberService clanMemberService,
                        @Value("${steam.api.key}") String key) {
        this.clanMemberService = clanMemberService;
        this.API_KEY = key;
    }

    public void getUserHoursAndExportToFile(@NotNull MessageReceivedEvent event) {
        log.info("Download hours for clan members");
        createLogFile();
        createCSVFile();
        try {
            setUsersList();
            if (users.isEmpty()) {
                event.getMessage().reply("Brak zapisanych użytkowników.").queue();
                log.info("No users to download");
                return;
            }
            event.getMessage().reply("Pobieram godziny. Za chwilę wyślę plik csv").queue();
            getUsersHours();
            saveDataToFile();
            sendResultCSV(event);
        } catch (Exception exception) {
            log.error("Exception", exception);
            sendLogFile(event);
        } finally {
            clearTempData();
            log.info("Download hours finish");
        }
    }

    private void clearTempData() {
        users.clear();
        logFiles.clear();
        logFiles = null;
        resultCSV.clear();
        resultCSV = null;
        log.info("Clear temp data");
    }

    private void sendLogFile(@NotNull MessageReceivedEvent event) {
        FileUpload fileUpload = FileUpload.fromData(logFiles.getFile());
        event.getMessage().reply("Wystąpił nieoczekiwany błąd").addFiles(fileUpload).queue();
    }

    private void sendResultCSV(@NotNull MessageReceivedEvent event) {
        logFiles.writeSeparatorToLogFile();
        logFiles.writeLineToFile("[INFO] - Wysyłanie pliku");
        FileUpload fileUpload = FileUpload.fromData(resultCSV.getFile());
        event.getMessage().replyFiles(fileUpload).queue();
        logFiles.writeLineToFile("[INFO] - Plik wysłany");
        log.info("File {} send to user {}", resultCSV.getFile().getName(), event.getAuthor());
    }

    private void createCSVFile() {
        resultCSV = new TempFiles("user-hours.csv");
        log.info("Created temp file - {}", resultCSV.getFile().getName());
    }

    private void createLogFile() {
        logFiles = new TempFiles("log-user-hours.txt");
        logFiles.writeLineToFile("Time: " + LocalDateTime.now(), false);
        log.info("Created temp file - {}", logFiles.getFile().getName());
    }

    private void setUsersList() {
        logFiles.writeSeparatorToLogFile();
        logFiles.writeLineToFile("[INFO] - Wczytywanie użytkowników");
        List<ClanMember> memberList = clanMemberService.findAll();
        setUserFromData(memberList);
        logFiles.writeLineToFile("[INFO] - Wczytano " + users.size() + " użytkowników");
        log.info("User list set. Size={}", users.size());
    }

    private void setUserFromData(@NotNull List<ClanMember> data) {
        for (ClanMember clanMember : data) {
            SteamUser user = new SteamUser(clanMember.getNick(), clanMember.getSteamId());
            users.add(user);
            log.debug("{}", user);
        }
    }

    /**
     * Getting hours for all users
     */
    private void getUsersHours() {
        logFiles.writeSeparatorToLogFile();
        logFiles.writeLineToFile("[INFO] - Pobieranie godzin");
        for (SteamUser user : users) {
            getHoursFromJSON(user);
        }
        logFiles.writeLineToFile("[INFO] - Pobrano godziny");
        log.info("Hours download for all users");
    }

    /**
     * Saving user to file "usersHours.csv"
     */
    private void saveDataToFile() {
        logFiles.writeSeparatorToLogFile();
        for (SteamUser user : users) {
            resultCSV.writeLineToFile(user.getName() + "," + user.getHours());
            logFiles.writeLineToFile(user.getName() + "," + user.getHours());
            log.debug("Wrote to file {}", user);
        }
        log.info("Hours saved to temp file {}", resultCSV.getFile().getName());
    }

    /**
     * Connect with Steam API, download data, setting hours for User
     *
     * @param user To which we set data.
     */
    private void getHoursFromJSON(@NotNull SteamUser user) {
        String responseContent = getResponseContent(user);
        if (responseContent == null) {
            logFiles.writeLineToFile("[WARN] - " + user.getName() + " - Nie można pobrać godzin.");
            log.info("{} - can not download hours", user.getName());
            return;
        }
        JSONObject jsonObject = new JSONObject(responseContent);
        logFiles.writeLineToFile("[USER] - " + user.getName());
        try {
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray games = response.getJSONArray("games");
            for (int i = 0; i < games.length(); i++) {
                JSONObject jsonGame = games.getJSONObject(i);
                int appId = jsonGame.getInt("appid");
                if (appId == APP_ID_SQUAD) {
                    int playtimeForever = jsonGame.getInt("playtime_forever");
                    user.setPlaytimeForever(playtimeForever);
                }
            }
        } catch (JSONException ignored) {
        }
        logFiles.writeLineToFile("> Pobrano");
        log.debug("Hours downloaded - {}", user);
    }

    /**
     * Getting content from steam API for {@link SteamUser}
     *
     * @param user {@link SteamUser} To which we download content.
     * @return String content from API.
     */
    private @Nullable String getResponseContent(@NotNull SteamUser user) {
        String urlString = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + API_KEY + "&steamid=" + user.getSteamId() + "&format=json";
        StringBuilder responseContent = new StringBuilder();
        URL url = getUrl(urlString);
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int status = connection.getResponseCode();

            if (status == 200) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                System.err.println("[WARN] - API Steam powered - BRAK DOSTĘPU.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            connection.disconnect();
        }
        return responseContent.toString();
    }


    /**
     * Convert String url to instance of {@link URL}
     *
     * @param url String of url
     * @return {@link URL}
     */
    @NotNull
    private URL getUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
