package event;

import embed.EmbedInfo;
import helpers.RangerLogger;
import model.MemberOfServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.util.ArrayList;
import java.util.List;

public class ActiveEvent {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private String name;
    private final String channelID;
    private final String messageID;
    private final List<MemberOfServer> mainList = new ArrayList<>();
    private final List<MemberOfServer> reserveList = new ArrayList<>();

    /**
     * @param channelID ID kanału na którym jest lista
     * @param messageID ID wiadomości na której jest embed z Lista.
     */
    public ActiveEvent(String channelID, String messageID) {
        this.channelID = channelID;
        this.messageID = messageID;
        setName();
    }

    public ActiveEvent(String channelID, String messageID, String name) {
        this.name = name;
        this.channelID = channelID;
        this.messageID = messageID;
    }

    private void setName() {
        try {
            JDA jda = Repository.getJda();
            Message message = jda.getTextChannelById(channelID).retrieveMessageById(messageID).complete();
            List<MessageEmbed> embeds = message.getEmbeds();
            name = embeds.get(0).getTitle();
            logger.info("Pobrałem nazwę {}", name);
        } catch (NullPointerException e) {
            logger.info("Brak aktywnego eventu. Nie mogę pobrać i ustawić nazwy.Event do usunięcia z bazy danych.");
        }
    }

    public String getChannelID() {
        return channelID;
    }

    public String getMessageID() {
        return messageID;
    }

    public List<MemberOfServer> getMainList() {
        return mainList;
    }

    public List<MemberOfServer> getReserveList() {
        return reserveList;
    }

    public String getName() {
        return name;
    }

    public void addToMainList(MemberOfServer member, ButtonInteractionEvent event) {
        if (checkMemberOnMainList(member)) {
            EmbedInfo.cantSignIn(event.getUser().getId());
        } else {
            removeFromReserveList(member.getUserID());
            mainList.add(member);
            AddPlayerDB(member, true);
            RangerLogger.info(member.getUserName() + " zapisał się na listę.", event.getChannel().getName());
        }
    }

    public void addToMainList(String userID, String userName, ButtonInteractionEvent event) {
        MemberOfServer memberMy = new MemberOfServer(userID, userName);
        addToMainList(memberMy, event);
    }

    public void addToReserveList(MemberOfServer member, ButtonInteractionEvent event) {
        if (checkMemberOnReserveList(member)) {
            EmbedInfo.cantSignInReserve(event.getUser().getId());
        } else {
            removeFromMainList(member.getUserID());
            reserveList.add(member);
            AddPlayerDB(member, false);
            RangerLogger.info(member.getUserName() + " zapisał się na listę rezerwową.", event.getChannel().getName());
            logger.info("Dodano do listy rezerwowej.");
        }
    }

    public void addToReserveList(String userID, String userName, ButtonInteractionEvent event) {
        MemberOfServer memberMy = new MemberOfServer(userID, userName);
        addToReserveList(memberMy, event);
    }

    public void removeFromMainList(String userID) {
        if (checkMemberOnMainList(userID)) {
            for (int i = 0; i < mainList.size(); i++) {
                if (mainList.get(i).getUserID().equalsIgnoreCase(userID)) {
                    mainList.remove(i);
                    removePlayerDB(userID);
                    logger.info("Usunieto z listy głównej");
                }
            }
        }
    }

    public void removeFromReserveList(String userID) {
        if (checkMemberOnReserveList(userID)) {
            for (int i = 0; i < reserveList.size(); i++) {
                if (reserveList.get(i).getUserID().equalsIgnoreCase(userID)) {
                    reserveList.remove(i);
                    removePlayerDB(userID);
                    logger.info("Usunieto z listy rezerwowej");
                }
            }
        }
    }

    private void AddPlayerDB(MemberOfServer member, boolean b) {
        EventDatabase edb = new EventDatabase();
        edb.addPlayer(member.getUserID(), member.getUserName(), b, messageID);
    }

    private void removePlayerDB(String userID) {
        EventDatabase edb = new EventDatabase();
        edb.removePlayer(userID, messageID);
    }

    private boolean checkMemberOnReserveList(MemberOfServer member) {
        return reserveList.stream().anyMatch(memberOfServer -> memberOfServer.getUserID().equalsIgnoreCase(member.getUserID()));
    }

    private boolean checkMemberOnMainList(MemberOfServer member) {
        return mainList.stream().anyMatch(memberOfServer -> memberOfServer.getUserID().equalsIgnoreCase(member.getUserID()));
    }

    public boolean checkMemberOnReserveList(String userID) {
        return reserveList.stream().anyMatch(memberOfServer -> memberOfServer.getUserID().equalsIgnoreCase(userID));
    }

    public boolean checkMemberOnMainList(String userID) {
        return mainList.stream().anyMatch(memberOfServer -> memberOfServer.getUserID().equalsIgnoreCase(userID));
    }


    public void removeFromEvent(String userID) {
        String userName = SearchAndGetUserName(userID);
        removeFromMainList(userID);
        removeFromReserveList(userID);
        if (userName != null) {
            RangerLogger.info(userName + " wypisał się z listy", getChannelName(channelID));
        } else {
            EmbedInfo.cantSignOut(userID);
        }
    }

    public boolean removeFromEventManually(String userID) {
        String userName = SearchAndGetUserName(userID);
        if (userName != null) {
            removeFromMainList(userID);
            removeFromReserveList(userID);
            RangerLogger.info(userName + " pomyślnie wykreślony z eventu. - [" + name + "]");
            return true;
        } else {
            RangerLogger.info("Nie ma użytkownika na liście z której próbowano go wykreślić. - [" + name + "]");
            return false;
        }
    }

    private String getChannelName(String channelID) {
        JDA jda = Repository.getJda();
        List<TextChannel> textChannels = jda.getTextChannels();
        for (TextChannel t : textChannels) {
            if (t.getId().equalsIgnoreCase(channelID)) {
                return t.getName();
            }
        }
        return null;
    }

    private String SearchAndGetUserName(String userID) {
        for (MemberOfServer m : mainList) {
            if (m.getUserID().equalsIgnoreCase(userID)) {
                return m.getUserName();
            }
        }
        for (MemberOfServer m : reserveList) {
            if (m.getUserID().equalsIgnoreCase(userID)) {
                return m.getUserName();
            }
        }
        return null;
    }

    public String getStringOfMainList() {
        if (mainList.size() > 0) {
            String result = "";
            for (int i = 0; i < mainList.size(); i++) {
                result += mainList.get(i).getUserNameWithoutRangers() + "\n";
            }
            return result;
        } else {
            return "-";
        }
    }

    public String getStringOfReserveList() {
        if (reserveList.size() > 0) {
            String result = "";
            for (int i = 0; i < reserveList.size(); i++) {
                result += reserveList.get(i).getUserNameWithoutRangers() + "\n";
            }
            return result;
        } else {
            return "-";
        }
    }

    public void addToMainList(MemberOfServer memberMy) {
        mainList.add(memberMy);
    }

    public void addToReserveList(MemberOfServer memberMy) {
        reserveList.add(memberMy);
    }

    public int getNumberOfSignIn() {
        return mainList.size() + reserveList.size();
    }

    public void sendInfoChanges(EventChanges whatChange, String dateTime) {
        RangerLogger.info("Zapisanych na glównej liście: [" + mainList.size() + "], Rezerwa: [" + reserveList.size() + "] - Wysyłam informację.", messageID);
        for (int i = 0; i < mainList.size(); i++) {
            String userID = mainList.get(i).getUserID();
            EmbedInfo.sendInfoChanges(userID, this.messageID, whatChange, dateTime);
        }
        for (int i = 0; i < reserveList.size(); i++) {
            String userID = reserveList.get(i).getUserID();
            EmbedInfo.sendInfoChanges(userID, this.messageID, whatChange, dateTime);
        }
    }
}
