package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import pl.mbrzozowski.ranger.games.Coin;
import pl.mbrzozowski.ranger.games.Dice;
import pl.mbrzozowski.ranger.games.Essa;
import pl.mbrzozowski.ranger.giveaway.GiveawayService;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.response.ResponseMessage;
import pl.mbrzozowski.ranger.role.RoleService;
import pl.mbrzozowski.ranger.stats.ServerStats;

import java.util.ArrayList;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;

@Slf4j
@Service
public class SlashCommandListener extends ListenerAdapter {

    private final RoleService roleService;
    private final ServerStats serverStats;
    private final GiveawayService giveawayService;

    public SlashCommandListener(RoleService roleService,
                                ServerStats serverStats,
                                GiveawayService giveawayService) {
        this.roleService = roleService;
        this.serverStats = serverStats;
        this.giveawayService = giveawayService;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            writeCommandData(commandData);
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info(event.getUser() + " - used slash command(command={})", event.getName());
        String name = event.getName();
        if (name.equalsIgnoreCase(ADD_ROLE_TO_RANGER)) {
            boolean isAdded = roleService.addRole(event.getOption(DISCORD_ROLE_OPTION_NAME_ID),
                    event.getOption(DISCORD_ROLE_OPTION_NAME_NAME),
                    event.getOption(DISCORD_ROLE_OPTION_NAME_DESCRIPTION));
            if (isAdded) {
                event.reply("Dodano rolę. Usuń starą listę i dodaj nową poleceniem !roles").setEphemeral(true).queue();
            } else {
                event.reply("Nie udało się dodać roli.").setEphemeral(true).queue();
            }
        } else if (name.equalsIgnoreCase(REMOVE_ROLE_FROM_RANGER)) {
            boolean isRemoved = roleService.removeRole(event.getOption(DISCORD_ROLE_OPTION_NAME_ID));
            if (isRemoved) {
                event.reply("Usunięto rolę. Usuń starą listę i dodaj nową poleceniem !roles").setEphemeral(true).queue();
            } else {
                event.reply("Nie udało się usunąć roli.").setEphemeral(true).queue();
            }
        } else if (name.equalsIgnoreCase(STEAM_PROFILE)) {
            if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                try {
                    OptionMapping steam64id = event.getOption("steam64id");
                    if (steam64id == null) {
                        return;
                    }
                    if (serverStats.connectUserToSteam(event.getUser().getId(), steam64id.getAsString())) {
                        ResponseMessage.connectSuccessfully(event);
                    } else {
                        ResponseMessage.connectUnSuccessfully(event);
                    }
                } catch (CannotCreateTransactionException exception) {
                    log.error("Cannot create transaction exception. " + exception.getMessage());
                    ResponseMessage.cannotConnectStatsDB(event);
                }
            } else {
                ResponseMessage.youCanLinkedYourProfileOnChannel(event);

            }
        } else if (name.equalsIgnoreCase(STATS)) {
            if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.CHANNEL_STATS)) {
                try {
                    if (serverStats.isUserConnected(event.getUser().getId())) {
                        serverStats.viewStatsForUser(event, event.getUser().getId(), event.getChannel().asTextChannel());
                    } else {
                        ResponseMessage.notConnectedAccount(event);
                    }
                } catch (CannotCreateTransactionException exception) {
                    log.error("Cannot create transaction exception. " + exception.getMessage());
                    ResponseMessage.cannotConnectStatsDB(event);
                }
            } else {
                ResponseMessage.youCanCheckStatsOnChannel(event);
            }
        } else if (name.equalsIgnoreCase(DICE)) {
            Dice.start(event);
        } else if (name.equalsIgnoreCase(COIN)) {
            Coin.start(event);
        } else if (name.equalsIgnoreCase(ESSA)) {
            Essa.start(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_CREATE)) {
            giveawayService.create(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_END)) {
            giveawayService.end(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_CANCEL)) {
            giveawayService.cancel(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_LIST)) {
            giveawayService.showActive(event);
        } else if (name.equalsIgnoreCase(GIVEAWAY_RE_ROLL)) {
            giveawayService.reRoll(event);
        } else if (name.equalsIgnoreCase(FIX_GIVEAWAY_EMBED)) {
            giveawayService.fixEmbed(event);
        }
    }

    private void writeCommandData(@NotNull ArrayList<CommandData> commandData) {
//        commandData.add(Commands.slash(ADD_ROLE_TO_RANGER,
//                        "Dodaje nową rolę do Ranger bota dzięki czemu użytkownicy serwera będą mogli sobie ją sami przypisać.")
//                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_ID, "Discord ID dodawanej roli", true)
//                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_NAME, "Nazwa dodawanej roli", true)
//                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_DESCRIPTION, "Opis dodawanej roli", false)
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
//        commandData.add(Commands.slash(REMOVE_ROLE_FROM_RANGER,
//                        "Usuwa rolę z Ranger bota. Użytkownik serwera nie będzie mógł samemu przypisać sobie usuniętej roli.")
//                .addOption(OptionType.STRING, "id", "Discord ID usuwanej roli", true)
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
//        commandData.add(Commands.slash(STEAM_PROFILE,
//                        "Link your discord account to your steam profile if you want view stats from our server.")
//                .addOption(OptionType.STRING, "steam64id", "Your steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/", true));
//        commandData.add(Commands.slash(STATS, "To view your stats from our server"));
//        commandData.add(Commands.slash(DICE, "Rzut kością do gry"));
//        commandData.add(Commands.slash(COIN, "Rzut monetą"));
//        commandData.add(Commands.slash(ESSA, "Sprawdza twój dzisiejszy poziom essy"));
        commandData.add(Commands.slash(GIVEAWAY_CREATE, "Tworzy giveaway na tym kanale"));
        commandData.add(Commands.slash(GIVEAWAY_END, "Kończy giveaway i losuje nagrody")
                .addOption(OptionType.INTEGER, GIVEAWAY_ID, "id giveawaya", false));
        commandData.add(Commands.slash(GIVEAWAY_CANCEL, "Kończy giveaway bez losowania nagród")
                .addOption(OptionType.INTEGER, GIVEAWAY_ID, "id giveawaya", false));
        commandData.add(Commands.slash(GIVEAWAY_LIST, "Pokazuje listę aktywnych giveawayów na serwerze"));
        commandData.add(Commands.slash(GIVEAWAY_RE_ROLL, "Losowanie nagród dla zakończonego giveawaya")
                .addOption(OptionType.INTEGER, GIVEAWAY_ID, "id giveawaya", false));
        commandData.add(Commands.slash(FIX_GIVEAWAY_EMBED, "Przywraca embed dla eventu")
                .addOption(OptionType.STRING, "id", "id wiadomości", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL)));
    }
}
