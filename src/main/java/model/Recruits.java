package model;

import embed.EmbedCloseChannel;
import embed.EmbedOpernChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class Recruits {

    private List<Recrut> activeRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Collection<Permission> permissions1 = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private Collection<Permission> permissionsTest = EnumSet.of(Permission.MESSAGE_WRITE);
    private final String CATEGORY_ID = "842886351346860112"; //Kategoria  Brzoza i Ranger testujo
    private final String PATH_ACTIVE_RECRUITS = "./src/main/resources/databaseFiles/ActiveRecruits.txt";
    private final String PATH_ACTIVE_RECRUITS_DIRECTORY = "./src/main/resources/databaseFiles";

    public void createChannelForNewRecrut(ButtonClickEvent event, String userName, String userID) {
        String idRadaKlanu = event.getGuild().getRolesByName("Rada Klanu", true).get(0).getId();
        String idDrill = event.getGuild().getRolesByName("Drill Instructor", true).get(0).getId();
        event.deferEdit().queue();
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CATEGORY_ID)) {
                event.getGuild().createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(event.getGuild().getPublicRole(), null, permissions1)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions1, null)
                        .setTopic(userID+";"+userName+";")
                        .queue(textChannel -> {
                            textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + "Drill Instructor" + "> oraz innych członków klanu.\n" +
                                    "<@&" + "Rada Klanu" + "> ").queue();
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.GREEN);
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                            builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                            builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                            builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                            textChannel.sendMessage(builder.build()).queue();
                            textChannel.sendMessage("Wkrótce się z Tobą skontaktujemy.").queue();
                            addUserToList(userID, userName, textChannel.getId());
                        });
            }
        }
        logger.info("Nowe podanie złożone. Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    public void initialize(JDA jda) {
        startUpList(jda);
    }


    public void newPodanie(ButtonClickEvent event) {
        String userName = event.getUser().getName();
        String userID = event.getUser().getId();
        if (!checkUser(userID)) {
            createChannelForNewRecrut(event, userName, userID);
        } else {
            event.deferEdit().queue();
            event.getJDA().retrieveUserById(userID).queue(user -> {
                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("NIE MOŻESZ ZŁOŻYĆ WIĘCEJ NIŻ JEDNO PODANIE!");
                    builder.setColor(Color.red);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setDescription("Zlożyłeś już podanie do naszego klanu i\n" +
                            "jesteś w trakcie rekrutacji.\n");
                    builder.addField("Jeżeli masz pytania w związku z Twoją rekrutacją", "", false);
                    builder.addField("1. Spradź kanały", "Znajdź kanał przypisany do twojej rekrutacji i napisz do nas.", false);
                    builder.addField("2.Nie widze kanału.", "Jeżeli nie widzisz kanału przypisanego do twojej rekrutacji skontaktuj się z nami bezpośrednio. Drill Instrutor -> Rada Klanu.", false);
                    privateChannel.sendMessage(builder.build()).queue();
                });
            });
        }
    }

    private void addUserToList(String userID, String userName, String buffCreatedChannelID) {
        Recrut member = new Recrut(userID, userName, buffCreatedChannelID);
        activeRecruits.add(member);
    }

    private void startUpList(JDA jda) {
        List<TextChannel> allTextChannels = jda.getTextChannels();
        for (int i = 0; i < allTextChannels.size(); i++) {
            String nameChannel = allTextChannels.get(i).getName();
            if (nameChannel.length()>=7){
                if (allTextChannels.get(i).getName().substring(0,7).equalsIgnoreCase("rekrut-")){
                    try{
                        if (!allTextChannels.get(i).getTopic().isEmpty()){
                            String topic = allTextChannels.get(i).getTopic();
                            String userID = "";
                            String userName = "";
                            String channelID = allTextChannels.get(i).getId();
                            int indexData = 0;
                            int subStringStartIndex = 0;
                            for (int j = 0; j < topic.length(); j++) {
                                if (topic.charAt(j) == ';'){
                                    if (indexData==0){
                                        //idusera
                                        userID = topic.substring(subStringStartIndex,j);
                                        subStringStartIndex = j +1;
                                        indexData++;
                                    }
                                    else if (indexData==1){
                                        //userName
                                        userName = topic.substring(subStringStartIndex,j);
                                    }
                                }
                            }
                            if (!userID.equalsIgnoreCase("") && !userName.equalsIgnoreCase("")){
                                Recrut recrut = new Recrut(userID,userName,channelID);
                                activeRecruits.add(recrut);
                            }

                        }
                    }catch (NullPointerException e){
                        logger.info("Brak danych by pobrać rekruta z kanału. Kanał prawdopodobnie utworzony ręcznie.");
                    }
                }
            }
        }
        logger.info("Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    private boolean checkUser(String userID) {
        for (Recrut member : activeRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    public void deleteChannelByID(String channleID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (channleID.equalsIgnoreCase(activeRecruits.get(i).getChannelID())) {
                activeRecruits.remove(i);
                logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            }
        }

    }

    public void closeChannel(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        boolean isRecruitChannel = isRecruitChannel(event);
        if (isRecruitChannel){
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member,null,permissions1).queue();
                    new EmbedCloseChannel(event);
                    logger.info("Kanał zamkniety: {} , userName: {}, userID: {}",event.getChannel().getName(),user.getName(),user.getId());
                });
            });
        }

    }

    private int getIndexOfRecrut(GuildMessageReceivedEvent event) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        boolean isRecruitChannel = isRecruitChannel(event);
        if (isRecruitChannel){
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member,permissions1,null).queue();
                    new EmbedOpernChannel(event);
                    logger.info("Kanał otwarty: {} , userName: {}, userID: {}",event.getChannel().getName(),user.getName(),user.getId());
                });
            });
        }
    }

    public boolean isRecruitChannel(GuildMessageReceivedEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isRecruitChannel(TextChannelUpdateTopicEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isRecruitChannel(TextChannelUpdateNameEvent event) {
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(GuildMessageReceivedEvent event){
        for (Recrut ar:activeRecruits){
            if (ar.getChannelID().equalsIgnoreCase(event.getChannel().getId())){
                return ar.getUserID();
            }
        }
        return "-1";
    }



}
