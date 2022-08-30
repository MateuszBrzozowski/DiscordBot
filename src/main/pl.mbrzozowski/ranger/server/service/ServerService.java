package ranger.server.service;

import ranger.embed.EmbedInfo;
import ranger.embed.EmbedSettings;
import ranger.event.ButtonClickType;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.ComponentService;
import ranger.helpers.RoleID;
import ranger.model.MemberWithPrivateChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;
import ranger.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class ServerService {

    private Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    private List<MemberWithPrivateChannel> reports = new ArrayList<>();

    public void initialize() {
        pullUsersFromDatabase();
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        String userName = event.getUser().getName();
        if (!isUserOnList(userID)) {
            createChannel(userID, userName, buttonType);
        } else {
            EmbedInfo.cantCreateServerServiceChannel(userID);
        }
    }

    public boolean isChannelOnList(String channelID) {
        for (MemberWithPrivateChannel l : reports) {
            if (l.getChannelID().equalsIgnoreCase(channelID)) {
                return true;
            }
        }
        return false;
    }

    public void closeChannel(MessageReceivedEvent event) {
        TextChannelManager manager = event.getTextChannel().getManager();
        String channelID = event.getChannel().getId();
        String userID = getUserID(channelID);
        Member member = event.getGuild().getMemberById(userID);
        if (member != null) {
            manager.putPermissionOverride(member, null, permissions);
            manager.queue();
        }
        EmbedInfo.closeChannel(event.getAuthor().getId(), event.getChannel());
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        ComponentService.disableButtons(event.getChannel().getId(), event.getMessageId());
        TextChannelManager manager = event.getTextChannel().getManager();
        String channelID = event.getTextChannel().getId();
        String userID = getUserID(channelID);
        Member member = event.getGuild().getMemberById(userID);
        if (member != null) {
            manager.putPermissionOverride(member, null, permissions);
            manager.queue();
        }
        EmbedInfo.closeChannel(event.getUser().getId(), event.getTextChannel());
    }

    public void removeChannel(@NotNull ButtonInteractionEvent event) {
        removeUserFromList(event.getChannel().getId());
    }

    public void removeUserFromList(String channelID) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getChannelID().equalsIgnoreCase(channelID)) {
                reports.remove(i);
                ServerServiceDatabase ssdb = new ServerServiceDatabase();
                ssdb.removeRecord(channelID);
            }
        }
    }

    private void pullUsersFromDatabase() {
        ServerServiceDatabase ssdb = new ServerServiceDatabase();
        ResultSet resultSet = ssdb.pullAllUsers();
        this.reports.clear();

        List<MemberWithPrivateChannel> memberServerServicesToDeleete = new ArrayList<>();
        List<TextChannel> allTextChannels = Repository.getJda().getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) {
                        break;
                    } else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        MemberWithPrivateChannel m = new MemberWithPrivateChannel(userID, userName, channelID);
                        boolean isActive = false;
                        for (TextChannel tc : allTextChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            reports.add(m);
                        } else {
                            memberServerServicesToDeleete.add(m);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        for (MemberWithPrivateChannel m : memberServerServicesToDeleete) {
            ssdb.removeRecord(m.getChannelID());
        }
    }

    private String getUserID(String channelID) {
        for (MemberWithPrivateChannel r : reports) {
            if (r.getChannelID().equalsIgnoreCase(channelID)) {
                return r.getUserID();
            }
        }
        return null;
    }

    private void createChannel(String userID, String userName, ButtonClickType buttonType) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_SERVER);
        String channelName = channelNamePrefix(buttonType) + userName;
        guild.createTextChannel(channelName, category)
                .addPermissionOverride(guild.getPublicRole(), null, permissions)
                .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.SERVER_ADMIN), permissions, null)
                .addRolePermissionOverride(Long.parseLong(RoleID.MODERATOR), permissions, null)
                .queue(channel -> {
                    sendEmbedStartChannel(channel, buttonType);
                    addUserToList(userID, userName, channel.getId(), buttonType);
                });
    }

    private void sendEmbedStartChannel(TextChannel channel, ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT:
                EmbedInfo.sendEmbedReport(channel);
                break;
            case UNBAN:
                EmbedInfo.sendEmbedUnban(channel);
                break;
            case CONTACT:
                EmbedInfo.sendEmbedContact(channel);
                break;
        }
    }

    private String channelNamePrefix(ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT:
                return EmbedSettings.BOOK_RED + "┋report-";
            case UNBAN:
                return EmbedSettings.BOOK_BLUE + "┋unban-";
            case CONTACT:
                return EmbedSettings.BOOK_GREEN + "┋contact-";
            default:
                return "";
        }
    }

    private void addUserToList(String userID, String userName, String channelID, ButtonClickType buttonType) {
        MemberWithPrivateChannel member = new MemberWithPrivateChannel(userID, userName, channelID);
        reports.add(member);
        ServerServiceDatabase ssdb = new ServerServiceDatabase();
        ssdb.addNewUser(userID, channelID, userName);
    }

    private boolean isUserOnList(String userID) {
        for (MemberWithPrivateChannel m : reports) {
            if (m.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }
}
