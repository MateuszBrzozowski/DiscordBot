package recrut;

import database.DBConnector;
import embed.*;
import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.RoleID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class Recruits {

    private List<Recrut> activeRecruits = new ArrayList<>();
    private List<Recrut> thinkingRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE);
    private Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);
    private final RangerLogger rangerLogger = new RangerLogger();

    /**
     * @param userName Nazwa użytkownika
     * @param userID   ID użytkownika
     */
    public void createChannelForNewRecrut(String userName, String userID) {
        JDA jda = RangerBot.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        List<Category> categories = guild.getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                guild.createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                        .queue(textChannel -> {
                            textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + RoleID.DRILL_INSTRUCTOR_ID + "> oraz innych członków klanu.\n" +
                                    "<@&" + RoleID.RADA_KLANU + "> ").queue();
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.GREEN);
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                            builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                            builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                            builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                            textChannel.sendMessage(builder.build()).queue();
                            textChannel.sendMessage("Wkrótce skontaktuje się z Tobą Drill. Oczekuj na wiadomość.").queue();
                            addUserToList(userID, userName, textChannel.getId());
                        });
            }
        }
        logger.info("Nowe podanie złożone.");
    }

    public void initialize(JDA jda) {
        startUpList(jda);
    }


    public void newPodanie(ButtonClickEvent event) {
        String userName = event.getUser().getName();
        String userID = event.getUser().getId();
        event.deferEdit().queue();
        if (!checkUser(userID)) {
            if (!checkThinkingUser(userID)) {
                if (!RoleID.isRoleAnotherClanButtonClick(event)) {
                    if (!RoleID.isRoleButtonClick(event, RoleID.CLAN_MEMBER_ID)) {
                        confirmMessage(userID, userName);
                    } else new EmbedYouAreClanMember(event);
                } else new EmbedYouAreInClan(event);
            }
        } else new EmbedYouHaveRecrutChannel(event);
    }

    private void confirmMessage(String userID, String userName) {
        rangerLogger.info("Użytkownik [" + userName + "] chce złożyć podanie.");
        thinkingRecruits.add(new Recrut(userID, userName));
        JDA jda = RangerBot.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle("Potwierdź czy chcesz złożyć podanie?");
            builder.setDescription("Po potwierdzeniu rozpocznie się Twój okres rekrutacyjny w naszym klanie. Skontaktuję się z Tobą jeden z naszych Drillów aby wprowadzić Cię" +
                    " w nasze szeregi. Poprosimy również o wypełnienie krótkiego formularza.");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessage(builder.build()).setActionRow(Button.success("recrutY", "Potwierdzam"), Button.danger("recrutN", "Rezygnuję")).queue(message -> {
                Thread timer = new Thread(() -> {
                    logger.info("Nowy wątek");
                    try {
                        Thread.sleep(1000 * 60 * 2); //2 minuty oczekiwania na odpowiedź
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (userIsThinking(userID) >= 0) {
                        logger.info("User dalej nie odpowiedział.");
                        cancel(userID, privateChannel, message.getId());
                        disableButtons(privateChannel, message.getId());
                    }
                    logger.info("kończę oczekiwanie");
                });
                timer.start();
            });
        });
    }

    public void confirm(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            createChannelForNewRecrut(thinkingRecruits.get(index).getUserName(), userID);
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    public void cancel(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            rangerLogger.info("Użytkownik [" + thinkingRecruits.get(index).getUserName() + "] zrezygnował ze złożenia podania.");
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    private void sendMessageBotReload(String userID) {
        JDA jda = RangerBot.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("UPS! Coś poszło nie tak. Jeżeli chcesz złóż ponownie podanie.").queue();
        });
    }

    /**
     * @param userID ID użytkowanika który składa podanie
     * @return Zwraca index na liście thinkingRecruits, jeżeli użytkownik dostał wiadomość z prośbą o potwierdzenie
     * złożenie podania i dalej ma możliwość akceptacji; W innym przypadku zwraca -1
     */
    private int userIsThinking(String userID) {
        for (int i = 0; i < thinkingRecruits.size(); i++) {
            if (userID.equalsIgnoreCase(thinkingRecruits.get(i).getUserID())) {
                return i;
            }
        }
        return -1;
    }

    public void disableButtons(MessageChannel channel, String messageID) {
        channel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessage(messageEmbed).setActionRow(Button.success("recrutY", "Potwierdzam").asDisabled(), Button.danger("recrutN", "Rezygnuję").asDisabled()).queue();
        });
    }

    private void addUserToList(String userID, String userName, String channelID) {
        Recrut member = new Recrut(userID, userName, channelID);
        activeRecruits.add(member);
        addUserToDataBase(userID, userName, channelID);
    }

    private void addUserToDataBase(String userID, String userName, String channelID) {
        String query = "INSERT INTO `recruts` (`userID`, `userName`, `channelID`) VALUES (\"%s\",\"%s\",\"%s\")";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, userID, userName, channelID));
    }

    private void startUpList(JDA jda) {
        ResultSet resultSet = getAllRecrutFromDataBase();
        List<Recrut> recrutsToDeleteDataBase = new ArrayList<>();
        this.activeRecruits.clear();
        List<TextChannel> allTextChannels = jda.getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        Recrut recrut = new Recrut(userID, userName, channelID);
                        boolean isActive = false;
                        for (TextChannel tc : allTextChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            activeRecruits.add(recrut);
                        } else {
                            recrutsToDeleteDataBase.add(recrut);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        for (Recrut rc : recrutsToDeleteDataBase) {
            RemoveRecrutFromDataBase(rc.getChannelID());
        }
//        rangerLogger.Info(String.format("Aktywnych rekrutacji: %d",activeRecruits.size()));
        logger.info("Aktywnych rekrutacji: {}", activeRecruits.size());
    }

    private ResultSet getAllRecrutFromDataBase() {
        String query = "SELECT * FROM `recruts`";
        DBConnector connector = new DBConnector();
        ResultSet resultSet = null;
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            logger.info("Brak tabeli recruts w bazie danych -> Tworze tabele.");
            String queryCreate = "CREATE TABLE recruts(" +
                    "userID VARCHAR(30) PRIMARY KEY," +
                    "userName VARCHAR(30) NOT NULL," +
                    "channelID VARCHAR(30) NOT NULL)";
            connector.executeQuery(queryCreate);
        }
        return resultSet;
    }

    private boolean checkUser(String userID) {
        for (Recrut member : activeRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkThinkingUser(String userID) {
        for (Recrut member : thinkingRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    public void deleteChannelByID(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (channelID.equalsIgnoreCase(activeRecruits.get(i).getChannelID())) {
                removeRoleFromUserID(activeRecruits.get(i).getUserID());
                activeRecruits.remove(i);
                RemoveRecrutFromDataBase(channelID);
                logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            }
        }
    }

    public void removeRoleFromUserID(String userID) {
        JDA jda = RangerBot.getJda();
        jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID).retrieveMemberById(userID).queue(member -> {
            List<Role> roles = member.getRoles();
            for (Role r : roles) {
                if (r.getId().equalsIgnoreCase(RoleID.RECRUT_ID)) {
                    member.getGuild().removeRoleFromMember(member, r).queue();
                    break;
                }
            }
        });
    }

    private void RemoveRecrutFromDataBase(String channelID) {
        String query = "DELETE FROM `recruts` WHERE channelID=\"%s\"";
        DBConnector connector = new DBConnector();
        connector.executeQuery(String.format(query, channelID));
    }

    public void closeChannel(GuildMessageReceivedEvent event) {
        boolean isRecruitChannel = isRecruitChannel(event.getChannel().getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member, null, permissions).queue();
                    new EmbedCloseChannel(event);
                    logger.info("Kanał zamkniety: {} , userName: {}, userID: {}", event.getChannel().getName(), user.getName(), user.getId());
                });
            });
        }

    }

    private int getIndexOfRecrut(GuildMessageReceivedEvent event) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(event.getChannel().getId())) {
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(GuildMessageReceivedEvent event) {
        boolean isRecruitChannel = isRecruitChannel(event.getChannel().getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(event);
            event.getJDA().retrieveUserById(activeRecruits.get(indexOfRecrut).getUserID()).queue(user -> {
                event.getGuild().retrieveMember(user).queue(member -> {
                    event.getChannel().getManager().putPermissionOverride(member, permissions, null).queue();
                    new EmbedOpernChannel(event);
                    logger.info("Kanał otwarty: {} , userName: {}, userID: {}", event.getChannel().getName(), user.getName(), user.getId());
                });
            });
        }
    }

    public boolean isRecruitChannel(String channelID) {
        for (Recrut ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(String channelID) {
        for (Recrut ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public void deleteChannel(GuildMessageReceivedEvent event) {
        logger.info("Kanał jest kanałem rekrutacyjnym.");
        new EmbedRemoveChannel(event);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteChannelByID(event.getChannel().getId());
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().reason("Rekrutacja zakończona").queue();
            logger.info("Kanał został usunięty.");
        });
        thread.start();
    }

    private String getUserNameFromID(String userID) {
        JDA jda = RangerBot.getJda();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            if (guild.getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
                String name = guild.getMemberById(userID).getNickname();
                if (name == null) {
                    name = guild.getJDA().getUserById(userID).getName();
                }
                return name;
            }
        }
        return null;
    }


    /**
     * @param nickRecrut Nick rekruta który może sobie ustawić
     * @param channel    Kanał rekruta
     * @param drill      Drill który przyjął na rekrutację
     */
    public void acceptRecrut(String nickRecrut, TextChannel channel, User drill) {
        if (isRecruitChannel(channel.getId())) {
        channel.sendMessage("<@" + "getRecruitIDFromChannelID(channel.getId())" + "> Kilka ważnych informacji na początek!").queue();
        welcome(channel, nickRecrut);
        recrutingTimeInfo(channel);
        breakTimeInfo(channel);
        clanTagInfo(channel);
        rankInfo(channel);
        playingSquad(channel);
        useTSAndDiscord(channel);
        whoIsWho(channel);
        signature(channel, drill);
        }
    }

    private void useTSAndDiscord(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.addField("TS - audio / Discord - text","Do komunikacji głosowej używamy ts. Wbijaj zawsze gdy grasz. Nawet jeżeli nikogo nie ma, wejdź na kanał. Może ktoś do Ciebie dołączy.\n" +
                "Discorda używamy natomiast do komunikacji tekstowej.",false);
        channel.sendMessage(builder.build()).queue();
    }

    private void clanTagInfo(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.addField("Kultura i szacunek do innych!","Grając na serwerze z tagiem klanu, pamiętaj że jesteś jego wizytówką! Jako grupa uważamy się " +
                "za kulturalnych i dojrzałych – niech taki obraz Rangersów trwa.",false);
        channel.sendMessage(builder.build()).queue();
    }

    private void rankInfo(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Rangi w klanie");
        builder.setDescription("Rangi są uproszczonym układem znanym z US Army. Ranga jest wyznaczana na zasadzie algorytmu, " +
                "biorącego pod uwagę trzy aspekty, które są następnie ważone:\n" +
                "- staż w klanie\n" +
                "- czas gry w SQ\n" +
                "- liczba rund rozegranych w oficjalnych meczach\n" +
                "Aktualizacja co miesiąc");
        channel.sendMessage(builder.build()).queue();
    }

    private void playingSquad(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Granie w squada");
        builder.addField("Codzienne granie", "Głównie wieczorami schodzimy się około godziny 19-20.", false);
        builder.addField("Zorganizowane granie", "Od czasu do czasu organizujemy zorganizowane granie w większej ekipie. " +
                "Informujemy was odpowiednio wcześniej. Schodzimy się o wybranej godzinie, wybieramy serwer i gramy!", false);
        builder.addField("Mecze międzyklanowe", "Głównie bierzemy udział w niedzielnych CCFN (Community Clan Fight Night) " +
                "i sobotnich SCFC (Squad Clan Fight Community). Gramy w meczach międzypaństowwych jeżeli takie są organizowane. - Obecnie przerwa wakakcyjna.", false);
        builder.addField("Eventy międzyklanowe", "Bierzemy czynny udział w eventach organizowanych przez polskie jak i zagraniczne klany. " +
                "Czasami organizujemy sami takie eventy dla polskich graczy z klanów jak i również dla ludzi niezrzeszonych.", false);
        builder.addField("Eventy klanowe", "Organizujemy zamknięte eventy klanowe dla złapania luzu i dobrej zabawy między innymi AIM Master, RAT Race", false);
        builder.addField("Szkolenia", "Organizujemy zamknięte nieobowiązkowe szkolenia dla naszych członków i rekrutów. " +
                "Szkolenia z SquadLeadera, Medyka, HAT/LAT, Obsługi i rozpoznawania pojazdów, Podchodzenia i zdobywania punktów i wiele innych.", false);
        builder.addField("RASP", "czyli **Ranger Assessment and Selection Program** - Obowiązkowe szkolenie dla każdego nowego członka składające się z dwóch części." +
                " Wymagane do awansu na stopnie NCO.", false);
        builder.setFooter("Udział w oficjalnych meczach dostępny po przejściu pozytywnie rekrtuacji.");
        channel.sendMessage(builder.build()).queue();
    }

    private void breakTimeInfo(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Przerwa/Wyjazd/Brak chęci?");
        builder.setDescription("Jeżeli planujesz podczas rekrutacji przerwę w graniu dłuższą niż tydzień z jakiegokolwiek powodu daj nam znać! \n" +
                "Nie masz ochoty już z nami grać? OK, żaden problem. Tylko nie zostawiaj nas bez wiadomości. Chcemy wiedzieć na czym stoimy.");
        channel.sendMessage(builder.build()).queue();
    }

    private void recrutingTimeInfo(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Ile trwa rekrutacja?");
        builder.setDescription("Rekrutacja trwa zwykle około 1-1,5 misiąca. Nie mamy zakresu godzin jaki musisz przegrać jednak jako rekrut warto być aktywnym. Daj się poznać!");
        channel.sendMessage(builder.build()).queue();
    }

    private void whoIsWho(TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setTitle("Kto jest kim?");
        builder.addField("Clan Leader", "Bruder", false);
        builder.addField("Vice Clan Leader", "Lodyga, Brzozaaa", false);
        builder.addField("Drill Intructor", "Jurand, Kapibałe", false);
        builder.addField("Serwer Administrator", "LuQeRo", false);
        channel.sendMessage(builder.build()).queue();
    }

    private void welcome(TextChannel channel, String nickRecrut) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setTitle("Witaj na rekrutacji do klanu RANGERS POLSKA");
        builder.setDescription("Dziękujemy za złożenie podania i wypełnienie formularza.");
        builder.addField("Możesz ustawić sobie nick: " + nickRecrut, "", false);
        channel.sendMessage(builder.build()).queue();
    }

    private void signature(TextChannel channel, User drill) {
        String drillName = getUserNameFromID(drill.getId());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail("https://cdn.icon-icons.com/icons2/2622/PNG/512/gui_signature_icon_157586.png");
        builder.addField("Przyjął na rekrutację", drillName, false);
        builder.addField("", "Cześć! W trakcie Twojej rekrutacji jestem twoim pierwszym kontaktem w razie jakichkolwiek problemów, pytań itd." +
                " Możesz napisać do mnie bezpośrednio na priv lub tutaj na kanale.\n" +
                "Mam nadzieję, że przeczytałeś powyższe informacje. Jeżeli masz do nich lub ogólnie jakieś pytania. Proszę zadaj mi je tutaj lub łap mnie na TS. Powodzenia i do usłyszenia!", false);
        channel.sendMessage(builder.build()).queue();
    }

}
