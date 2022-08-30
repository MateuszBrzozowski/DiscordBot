package ranger.counter;

import ranger.embed.EmbedSettings;
import ranger.helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import ranger.Repository;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

public class Counter {

    private static final String FOOTER = "Liczydło © od 11.02.2022r.";
    private static final String TITLE = "Najaktywniejsi na discordzie";
    private final List<CounterUser> users = new ArrayList<>();
    private boolean isChange = false;

    public void initialize() {
        pullAllUsersFromDataBase();
        Timer timer = new Timer();
        LocalDateTime time = LocalDateTime.now().plusMinutes(5);
        Date dateTimeNow = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
        timer.schedule(task(), dateTimeNow, 1000 * 60 * 5);
    }

    public void showTopThree(String channelID) {
        JDA jda = Repository.getJda();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(TITLE);
        builder.addField(getFirstPlace(), getSecondThirdPlace(), false);
        builder.setFooter(FOOTER);
        jda.getTextChannelById(channelID).sendMessageEmbeds(builder.build()).queue();
    }

    public void showTopTen(String channelID) {
        JDA jda = Repository.getJda();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(TITLE);
        builder.addField(getFirstThreePlace(), getPlaceFromFourToTen(), false);
        builder.setFooter(FOOTER);
        jda.getTextChannelById(channelID).sendMessageEmbeds(builder.build()).queue();
    }

    public void showUser(String userID, String channelID) {
        JDA jda = Repository.getJda();
        List<CounterUser> sortedUsers = sortedUsers();
        int place = getPlace(sortedUsers, userID);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle(TITLE);
        builder.addField("Nazwa użytkownika : " + Users.getUserNicknameFromID(userID),
                "Liczba wiadomości: **" + users.get(getIndex(userID)).getCountMsgAll() + "**\n" +
                        "Procent (z wszystkich wiadomości klanowiczów): **" + getPercent(userID) + "**",
                false);
        builder.addField("Twoje miejsce", String.valueOf(place), false);
        if (place > 1) {
            CounterUser user = sortedUsers.get(place - 2);
            builder.addField("Przed Tobą",
                    getLine(place - 1, sortedUsers) + "\n" +
                            "Liczba wiadomości: **" + user.getCountMsgAll() + "**\n" +
                            "Brakuje Ci: " + msgBeforeUser(sortedUsers, place) + " wiadomośći.",
                    false);
        }
        if (place < sortedUsers.size()) {
            CounterUser user = sortedUsers.get(place);
            builder.addField("Za Tobą",
                    getLine(place + 1, sortedUsers) + "\n" +
                            "Liczba wiadomości: **" + user.getCountMsgAll() + "**\n" +
                            "Przewaga: " + msgAfterUser(sortedUsers, place) + " wiadomośći.",
                    false);
        }
        builder.setFooter(FOOTER);
        jda.getTextChannelById(channelID).sendMessageEmbeds(builder.build()).queue();
    }

    private int msgAfterUser(List<CounterUser> sortedUsers, int place) {
        CounterUser user = sortedUsers.get(place - 1);
        CounterUser userAfter = sortedUsers.get(place);
        return user.getCountMsgAll() - userAfter.getCountMsgAll();
    }

    private int msgBeforeUser(List<CounterUser> sortedUsers, int place) {
        CounterUser user = sortedUsers.get(place - 1);
        CounterUser userBefore = sortedUsers.get(place - 2);
        return userBefore.getCountMsgAll() - user.getCountMsgAll();
    }

    private String getPercent(String userID) {
        float sum = getSum();
        float msgUser = users.get(getIndex(userID)).getCountMsgAll();
        return Math.round(100 * msgUser / sum) + "%";

    }

    private float getSum() {
        int result = 0;
        for (CounterUser user : users) {
            result += user.getCountMsgAll();
        }
        return result;
    }

    private int getPlace(List<CounterUser> sortedUsers, String userID) {
        for (int i = 0; i < sortedUsers.size(); i++) {
            if (sortedUsers.get(i).getUserID().equalsIgnoreCase(userID)) {
                return i + 1;
            }
        }
        return -1;
    }

    private String getLine(int place, List<CounterUser> sortedUsers) {
        int index = place - 1;
        if (sortedUsers.size() > index) {
            CounterUser user = sortedUsers.get(index);
            String nickname = Users.getUserNicknameFromID(user.getUserID());
            int msgAll = user.getCountMsgAll();
            return place + ". " + nickname + " - (" + msgAll + ") - " + getPercent(user.getUserID());
        }
        return place + ". ";
    }

    private String getFirstPlace() {
        List<CounterUser> sortedUsers = sortedUsers();
        return getLine(1, sortedUsers);
    }

    private String getSecondThirdPlace() {
        List<CounterUser> sortedUsers = sortedUsers();
        return getLine(2, sortedUsers) + "\n" + getLine(3, sortedUsers);
    }

    private String getFirstThreePlace() {
        List<CounterUser> sortedUsers = sortedUsers();
        String result = "";
        for (int i = 1; i <= 3; i++) {
            result += getLine(i, sortedUsers) + "\n";
        }
        return result;
    }

    private String getPlaceFromFourToTen() {
        List<CounterUser> sortedUsers = sortedUsers();
        String result = "";
        for (int i = 4; i <= 10; i++) {
            result += getLine(i, sortedUsers) + "\n";
        }
        return result;
    }

    private List<CounterUser> sortedUsers() {
        List<CounterUser> sortedList = new ArrayList<>();
        if (users.size() > 1) {
            sortedList.add(users.get(0));
            for (int i = 1; i < users.size(); i++) {
                int whereInsert = i;
                for (int j = i - 1; j >= 0; j--) {
                    if (users.get(i).getCountMsgAll() > sortedList.get(j).getCountMsgAll()) {
                        whereInsert = j;
                    } else {
                        break;
                    }
                }
                sortedList.add(whereInsert, users.get(i));
            }
        } else {
            sortedList = users;
        }
        return sortedList;
    }

    private TimerTask task() {
        return new TimerTask() {
            @Override
            public void run() {
                if (isChange) {
                    CounterDatabase counterDatabase = new CounterDatabase();
                    for (CounterUser cu : users) {
                        counterDatabase.updateUser(cu.getUserID(), cu.getCountMsgAll());
                    }
                    isChange = false;
                }
            }
        };
    }

    public void userPlusOneMsg(String userID) {
        int index = getIndex(userID);
        if (index == -1) {
            CounterUser counterUser = new CounterUser(userID);
            counterUser.addUserToDatabase();
            users.add(0, counterUser);
            users.get(0).plusOne();
        } else {
            users.get(index).plusOne();
        }
        isChange = true;
    }

    private int getIndex(String userID) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserID().equalsIgnoreCase(userID)) {
                return i;
            }
        }
        return -1;
    }

    private void pullAllUsersFromDataBase() {
        CounterDatabase cdb = new CounterDatabase();
        ResultSet resultSet = cdb.pullAllUsers();
        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    }
                    String userId = resultSet.getString("userID");
                    int msgAll = resultSet.getInt("msgAll");
                    CounterUser user = new CounterUser(userId, msgAll);
                    users.add(user);
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }
}
