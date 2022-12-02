package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.repository.main.ClientRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ServerService {

    private final ClientRepository clientRepository;
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

    public ServerService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        if (!userHasReport(event.getUser().getId())) {
            createChannel(event, buttonType);
            event.deferEdit().queue();
        } else {
            ResponseMessage.cantCreateServerServiceChannel(event);
        }
    }

    public void closeChannel(@NotNull MessageReceivedEvent event) {
        Optional<Client> clientOptional = findByChannelId(event.getChannel().getId());
        if (clientOptional.isPresent()) {
            event.getMessage().delete().submit();
            Member member = event.getGuild().getMemberById(clientOptional.get().getUserId());
            if (member != null) {
                event
                        .getTextChannel()
                        .getManager()
                        .putPermissionOverride(member, null, permissions)
                        .queue();
            }
            EmbedInfo.closeServerServiceChannel(event.getAuthor().getId(), event.getChannel());
            clientCloseChannelSave(clientOptional.get());
        }
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        Optional<Client> clientOptional = clientRepository.findByChannelId(event.getChannel().getId());
        if (clientOptional.isPresent()) {
            Guild guild = event.getGuild();
            if (guild != null) {
                Member member = guild.getMemberById(clientOptional.get().getUserId());
                if (member != null) {
                    event.getTextChannel()
                            .getManager()
                            .putPermissionOverride(member, null, permissions)
                            .queue();
                }
                EmbedInfo.closeServerServiceChannel(event.getUser().getId(), event.getTextChannel());
                clientCloseChannelSave(clientOptional.get());
            }
        }
    }

    private void clientCloseChannelSave(@NotNull Client client) {
        client.setIsClose(true);
        client.setCloseTimestamp(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        clientRepository.save(client);
    }

    public void removeChannel(@NotNull ButtonInteractionEvent event) {
        deleteByChannelId(event.getChannel().getId());
    }

    private void createChannel(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        String userName = event.getUser().getName();
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
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
                            sendEmbedStartChannel(userID, channel, buttonType);
                            addUser(userID, userName, channel.getId());
                        });
            }
        }
    }

    private void sendEmbedStartChannel(String userID, TextChannel channel, @NotNull ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT -> EmbedInfo.sendEmbedReport(userID, channel);
            case UNBAN -> EmbedInfo.sendEmbedUnban(userID, channel);
            case CONTACT -> EmbedInfo.sendEmbedContact(userID, channel);
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
        Client client = Client.builder()
                .userId(userId)
                .channelId(channelId)
                .userName(userName)
                .isClose(false)
                .build();
        clientRepository.save(client);
    }

    private boolean userHasReport(String userID) {
        Optional<Client> client = clientRepository.findByUserId(userID);
        return client.isPresent();
    }

    public void deleteChannelById(String channelId) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelId);
        if (textChannel != null) {
            textChannel.delete().reason("Uplynął termin utrzymywania kanalu").queue();
            log.info("Deleted server service channel by id " + channelId);
        }
    }

    public void deleteByChannelId(String channelID) {
        clientRepository.deleteByChannelId(channelID);
    }

    public Optional<Client> findByChannelId(String channelID) {
        return clientRepository.findByChannelId(channelID);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }
}
