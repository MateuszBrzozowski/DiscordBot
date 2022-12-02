package pl.mbrzozowski.ranger.bot.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.role.RoleService;

import java.util.ArrayList;

import static pl.mbrzozowski.ranger.helpers.SlashCommands.*;

@Service
public class SlashCommandListener extends ListenerAdapter {

    private final RoleService roleService;

    public SlashCommandListener(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
//        ArrayList<CommandData> commandData = new ArrayList<>();
//        event.getGuild().updateCommands().queue();
        if (event.getGuild().getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            writeCommandData(commandData);
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
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
        }
    }

    private void writeCommandData(ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(ADD_ROLE_TO_RANGER,
                        "Dodaje nową rolę do Ranger bota dzięki czemu użytkownicy serwera będą mogli sobie ją sami przypisać.")
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_ID, "Discord ID dodawanej roli", true)
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_NAME, "Nazwa dodawanej roli", true)
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_DESCRIPTION, "Opis dodawanej roli", false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        commandData.add(Commands.slash(REMOVE_ROLE_FROM_RANGER,
                        "Usuwa rolę z Ranger bota. Użytkownik serwera nie będzie mógł samemu przypisać sobie usuniętej roli.")
                .addOption(OptionType.STRING, "id", "Discord ID usuwanej roli", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        commandData.add(Commands.slash(STEAM_PROFILE,
                        "Linked your discord account to your steam profile")
                .addOption(OptionType.STRING, "steam64id", "Your steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/", true));
        commandData.add(Commands.slash(STATS, "To view your stats"));
    }
}
