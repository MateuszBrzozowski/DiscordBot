package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.configuration.content.ContentService;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.guild.DeleteChannel;
import pl.mbrzozowski.ranger.guild.DeleteChannelById;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.model.TemporaryChannelsInteraction;
import pl.mbrzozowski.ranger.repository.main.ClientRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.server.service.transcription.TranscriptionService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static pl.mbrzozowski.ranger.guild.SlashCommands.SERVER_SERVICE_CLOSE_CHANNEL;
import static pl.mbrzozowski.ranger.guild.SlashCommands.SERVER_SERVICE_DELETE_CHANNEL;

@Slf4j
@Service
public class ServerService implements SlashCommand, TemporaryChannelsInteraction {

    private final TranscriptionService transcriptionService;
    private final ClientRepository clientRepository;
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    private final Collection<Permission> permissionsOnlyView = EnumSet.of(Permission.VIEW_CHANNEL);
    private final ContentService contentService;

    public ServerService(TranscriptionService transcriptionService, ClientRepository clientRepository, ContentService contentService) {
        this.transcriptionService = transcriptionService;
        this.clientRepository = clientRepository;
        this.contentService = contentService;
    }

    public boolean isTicket(@NotNull MessageReceivedEvent event) {
        Optional<Client> optional = findByChannelId(event.getChannel().getId());
        return optional.isPresent();
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event, @NotNull ButtonClickType buttonType) {
        log.info(event.getUser() + " - button type: " + buttonType);
        if (!userHasActiveReport(event.getUser().getId())) {
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
            closeChannel(clientOptional.get(), Users.getUserNicknameFromID(event.getAuthor().getId()));
        }
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        Optional<Client> clientOptional = clientRepository.findByChannelId(event.getChannel().getId());
        clientOptional.ifPresent(client -> closeChannel(client, Users.getUserNicknameFromID(event.getUser().getId())));
    }

    public void closeChannel(Client client, String whoClose) {
        Guild guild = RangersGuild.getGuild();
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(client.getChannelId());
            Member member = guild.getMemberById(client.getUserId());
            if (textChannel != null) {
                if (member != null) {
                    textChannel
                            .getManager()
                            .putPermissionOverride(member, null, permissions)
                            .queue(unused -> log.info("{} - Close channel - permission override for member:{}", textChannel, member));
                }
                EmbedInfo.closeServerServiceChannel(whoClose, textChannel);
                clientCloseChannelSave(client);
            }
        }
    }

    public void openChannel(Client client) {
        Guild guild = RangersGuild.getGuild();
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(client.getChannelId());
            Member member = guild.getMemberById(client.getUserId());
            if (textChannel != null) {
                if (member != null) {
                    textChannel
                            .getManager()
                            .putPermissionOverride(member, permissions, null)
                            .queue(unused -> log.info("{} - Open channel - permission override for member:{}", textChannel, member));
                }
                clientOpenChannelSave(client);
            }
        }
    }

    private void clientOpenChannelSave(@NotNull Client client) {
        client.setAutoClose(false);
        client.setIsClose(false);
        client.setCloseTimestamp(null);
        clientRepository.save(client);
        log.info("Client=({}) channel opened", client);
    }

    private void clientCloseChannelSave(@NotNull Client client) {
        client.setIsClose(true);
        client.setCloseTimestamp(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        clientRepository.save(client);
        log.info("Client=({}) channel closed", client);
    }

    private void createChannel(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        String userName = Users.getNickname(Objects.requireNonNull(event.getMember()));
        Guild guild = RangersGuild.getGuild();
        if (guild != null) {
            Category category = RangersGuild.getCategory(RangersGuild.CategoryId.SERVER);
            String channelName = channelNamePrefix(buttonType) + userName;
            if (category != null) {
                guild.createTextChannel(channelName, category)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.MODERATOR), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_COUNCIL), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.SERVER_ADMIN), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.SERVER_JUNIOR_ADMIN), permissionsOnlyView, null)
                        .queue(channel -> {
                            sendEmbedStartChannel(userID, channel, buttonType);
                            addUser(userID, userName, channel.getId());
                            transcriptionService.openTicket(channel.getId(), userName);
                        });
            }
        }
    }

    private void sendEmbedStartChannel(String userID, TextChannel channel, @NotNull ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT -> EmbedInfo.sendEmbedReport(userID, channel, contentService);
            case UNBAN -> EmbedInfo.sendEmbedUnban(userID, channel, contentService);
            case CONTACT -> EmbedInfo.sendEmbedContact(userID, channel, contentService);
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
                .autoClose(true)
                .build();
        clientRepository.save(client);
    }

    protected boolean userHasActiveReport(String userID) {
        List<Client> clients = clientRepository.findByUserId(userID);
        clients = clients.stream()
                .filter(client -> !client.getIsClose())
                .collect(Collectors.toList());
        return clients.size() != 0;
    }

    public Optional<Client> findByChannelId(String channelID) {
        return clientRepository.findByChannelId(channelID);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public List<Client> findByIsCloseTrue() {
        return clientRepository.findByIsCloseTrue();
    }

    public List<Client> findByAutoCloseTrue() {
        return clientRepository.findByAutoCloseTrue();
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SERVER_SERVICE_DELETE_CHANNEL.getName(), SERVER_SERVICE_DELETE_CHANNEL.getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addOption(OptionType.INTEGER, "days", "Po ilu dniach?", true));
        commandData.add(Commands.slash(SERVER_SERVICE_CLOSE_CHANNEL.getName(), SERVER_SERVICE_CLOSE_CHANNEL.getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                .addOption(OptionType.INTEGER, "days", "Po ilu dniach?", true));
    }

    public void openNoClose(@NotNull ButtonInteractionEvent event) {
        String channelId = event.getChannel().getId();
        Optional<Client> clientOptional = findByChannelId(channelId);
        if (clientOptional.isEmpty()) {
            event.deferEdit().queue();
            log.warn("Ticket not exists in DB");
            return;
        }
        openChannel(clientOptional.get());
        event.deferEdit().queue();
        event.getMessage().delete().queue();
    }

    @Override
    public void deleteChannelById(String channelId) {
        DeleteChannelById deleteChannelById = new DeleteChannelById(this);
        deleteChannelById.accept(channelId);
    }

    @Override
    public void deleteFromDBByChannelId(String channelID) {
        Optional<Client> optional = findByChannelId(channelID);
        if (optional.isPresent()) {
            clientRepository.deleteByChannelId(channelID);
            log.info("Deleted channel from DB for Server Service (channelId={})", channelID);
            transcriptionService.createAndSendTranscript(channelID);
        }
    }

    @Override
    public void deleteChannelAfterButtonClick(@NotNull ButtonInteractionEvent event) {
        EmbedInfo.removedChannel(event);
        DeleteChannel deleteChannel = new DeleteChannel(event.getChannelId(), this);
        new Timer().schedule(deleteChannel, 5000);
    }
}
