package model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class Recruits {

    private List<ActiveRecruits> activeRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void createChannelForNewRecrut(ButtonClickEvent event, String userName, String userID) {
        String nameStrefaRekruta = "Brzoza i Ranger testujo";
        Collection<Permission> permissions1 = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
        String idRadaKlanu = event.getGuild().getRolesByName("Rada Klanu", true).get(0).getId();
        String idDrill = event.getGuild().getRolesByName("Drill Instructor", true).get(0).getId();
        event.deferEdit().queue();
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getName().equals(nameStrefaRekruta)) {
                event.getGuild().createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(event.getGuild().getPublicRole(), null, permissions1)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions1, null)
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
        ActiveRecruits member = new ActiveRecruits(userID, userName, buffCreatedChannelID);
        addUserToFile(userID, userName, buffCreatedChannelID, true);
        activeRecruits.add(member);
    }

    private void startUpList(JDA jda) {
        try {
            FileReader reader = new FileReader("./src/main/resources/databaseFiles/ActiveRecruits.txt");
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                String userLine = scanner.nextLine();
                String userName = null;
                String userID = null;
                String channelID = null;
                int indexData = 0;
                int subStringStartIndex = 0;
                for (int i = 0; i < userLine.length(); i++) {
                    if (userLine.charAt(i) == ';') {
                        if (indexData == 0) {
                            //id
                            userID = userLine.substring(subStringStartIndex, i);
                            subStringStartIndex = i + 1;
                            indexData++;
                        } else if (indexData == 1) {
                            //name
                            userName = userLine.substring(subStringStartIndex, i);
                            subStringStartIndex = i + 1;
                            indexData++;
                        } else if (indexData == 2) {
                            //channelID
                            channelID = userLine.substring(subStringStartIndex, i);
                            break;
                        }
                    }
                }

                List<TextChannel> textChannels = jda.getTextChannels();
                logger.info(String.valueOf(textChannels.size()));
                logger.info(channelID);
                for (int i = 0; i < textChannels.size(); i++) {
                    if (channelID.equalsIgnoreCase(textChannels.get(i).getId())) {
                        ActiveRecruits mb = new ActiveRecruits(userID, userName, channelID);
                        activeRecruits.add(mb);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.info("Nie ma pliku z aktywnymi rekrutami. Tworze plik.");
            createFile("./src/main/resources/databaseFiles/ActiveRecruits.txt");
        }
        addAllUsersToFile();
        logger.info(String.valueOf(activeRecruits.size()));
    }

    private void createFile(String s) {
        File newFile = new File(s);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAllUsersToFile() {
        if (activeRecruits.size() == 0) {
            clearFile();
        } else {
            for (int i = 0; i < activeRecruits.size(); i++) {
                if (i == 0) {
                    addUserToFile(activeRecruits.get(i).getUserID(), activeRecruits.get(i).getUserName(), activeRecruits.get(i).getChannelID(), false);
                } else {
                    addUserToFile(activeRecruits.get(i).getUserID(), activeRecruits.get(i).getUserName(), activeRecruits.get(i).getChannelID(), true);
                }
            }
        }

    }

    private void clearFile() {
        try {
            FileWriter writer = new FileWriter("./src/main/resources/databaseFiles/ActiveRecruits.txt");
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addUserToFile(String userID, String userName, String buffCreatedChannelID, boolean append) {
        try {
            FileWriter writer = new FileWriter("./src/main/resources/databaseFiles/ActiveRecruits.txt", append);
            writer.write(userID + ";" + userName + ";" + buffCreatedChannelID + ";\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkUser(String userID) {
        for (ActiveRecruits member : activeRecruits) {
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
            }
        }
    }

}
