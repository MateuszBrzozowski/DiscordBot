package ranger.server.service;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.embed.EmbedSettings;
import ranger.event.ButtonClickType;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.ComponentService;
import ranger.helpers.RoleID;
import ranger.model.MemberWithPrivateChannel;
import ranger.response.ResponseMessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class ServerService {

    private final ClientRepository clientRepository;
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    //    private final List<MemberWithPrivateChannel> reports = new ArrayList<>();

    public ServerService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;

    }

    public void buttonClick(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        if (!userHasReport(event.getUser().getId())) {
            createChannel(event, buttonType);
        } else {
            ResponseMessage.cantCreateServerServiceChannel(event);
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

    private void createChannel(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        String userName = event.getUser().getName();
        Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_SERVER);
            String channelName = channelNamePrefix(buttonType) + userName;
            if (category != null) {
                guild.createTextChannel(channelName, category)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.SERVER_ADMIN), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.MODERATOR), permissions, null)
                        .queue(channel -> {
                            sendEmbedStartChannel(channel, buttonType);
                            addUser(userID, userName, channel.getId());
                        });
            }
        }
    }

    private void sendEmbedStartChannel(TextChannel channel, @NotNull ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT -> EmbedInfo.sendEmbedReport(channel);
            case UNBAN -> EmbedInfo.sendEmbedUnban(channel);
            case CONTACT -> EmbedInfo.sendEmbedContact(channel);
        }
    }

    private String channelNamePrefix(ButtonClickType buttonType) {
        return switch (buttonType) {
            case REPORT -> EmbedSettings.BOOK_RED + "┋report-";
            case UNBAN -> EmbedSettings.BOOK_BLUE + "┋unban-";
            case CONTACT -> EmbedSettings.BOOK_GREEN + "┋contact-";
            default -> "";
        };
    }

    private void addUser(String userId, String userName, String channelId) {
        Client client = new Client(null, userId, channelId, userName);
        clientRepository.save(client);
    }

    private boolean userHasReport(String userID) {
        Optional<Client> client = clientRepository.findByUserId(userID);
        return client.isPresent();
    }
}
