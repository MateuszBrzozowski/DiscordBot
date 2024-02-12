package pl.mbrzozowski.ranger.role;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.model.SlashCommand;
import pl.mbrzozowski.ranger.repository.main.RoleRepository;

import java.util.*;

import static pl.mbrzozowski.ranger.guild.SlashCommands.*;

@Slf4j
@Service
public class RoleService implements SlashCommand {

    private final RoleRepository roleRepository;
    private static final int MAX_ROLES = 25;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private void save(Role role) {
        roleRepository.save(role);
    }

    private void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    private Optional<Role> findByDiscordRoleId(String discordRoleId) {
        return roleRepository.findByDiscordId(discordRoleId);
    }

    @NotNull
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        addRoleCommand(commandData);
        addAddingRoleToBot(commandData);
        addRemoveRoleFromBot(commandData);
    }

    private void addAddingRoleToBot(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(ADD_ROLE_TO_RANGER.getName(),ADD_ROLE_TO_RANGER.getDescription())
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_ID.getName(), DISCORD_ROLE_OPTION_NAME_ID.getDescription(), true)
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_NAME.getName(), DISCORD_ROLE_OPTION_NAME_NAME.getDescription(), true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES)));
    }

    private void addRemoveRoleFromBot(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(REMOVE_ROLE_FROM_RANGER.getName(),REMOVE_ROLE_FROM_RANGER.getDescription())
                .addOption(OptionType.STRING, DISCORD_ROLE_OPTION_NAME_ID.getName(), "Discord ID usuwanej roli", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES)));
    }

    private void addRoleCommand(@NotNull ArrayList<CommandData> commandData) {
        List<Role> roles = findAll();
        if (checkAmountOfRoles(roles.size())) {
            log.info("Can not create role slash command. Roles amount={}", roles.size());
            return;
        }
        CommandData command = getCommand(roles);
        commandData.add(command);
        log.info("Created slash command for role");
    }

    @NotNull
    private Set<Choice> getChoices(@NotNull List<Role> roles) {
        Set<Choice> choices = new HashSet<>();
        for (Role role : roles) {
            Choice choice = new Choice(role.getName(), role.getName());
            choices.add(choice);
            log.debug("{}", choice);
        }
        return choices;
    }

    /**
     * @param amount of roles
     * @return true if roles size is 0 or more than MAX_ROLES=25; false when 0 < roles.size <= 25
     */
    private boolean checkAmountOfRoles(int amount) {
        if (amount == 0) {
            return true;
        } else {
            return amount > MAX_ROLES;
        }
    }

    public void addRole(@NotNull SlashCommandInteractionEvent event) {
        log.info("Adding role");
        OptionMapping optionId = event.getOption(DISCORD_ROLE_OPTION_NAME_ID.getName());
        OptionMapping optionName = event.getOption(DISCORD_ROLE_OPTION_NAME_NAME.getName());
        if (!isOptionsValidToAdd(event, optionId, optionName)) {
            return;
        }
        String id = Objects.requireNonNull(optionId).getAsString();
        String name = Objects.requireNonNull(optionName).getAsString();
        Role role = new Role(id, name);
        save(role);
        event.reply("Dodano rolę.").setEphemeral(true).queue();
        log.info("{} added", role);
        updateRoleList();
    }

    private void updateRoleList() {
        ArrayList<CommandData> commandData = new ArrayList<>();
        addRoleCommand(commandData);
        Guild guild = RangersGuild.getGuild();
        if (guild != null) {
            List<Role> roles = findAll();
            guild.upsertCommand(getCommand(roles)).queue();
        }
    }

    @NotNull
    private CommandData getCommand(List<Role> roles) {
        Set<Choice> choices = getChoices(roles);
        return Commands.slash(ROLE.getName(), ROLE.getDescription())
                .addOptions(new OptionData(OptionType.STRING, "role", "Select role")
                        .addChoices(choices)
                        .setRequired(true));
    }

    private boolean isOptionsValidToAdd(SlashCommandInteractionEvent event, OptionMapping optionId, OptionMapping optionName) {
        if (optionName == null || optionId == null) {
            event.reply("Wystąpił nieoczekiwany błąd. Skontaktuj się z <@642402714382237716>").queue();
            log.error("Null option id={}, name={}", optionId, optionName);
            return false;
        }
        if (optionName.getAsString().length() > Choice.MAX_NAME_LENGTH || optionName.getAsString().length() == 0) {
            event.reply("Nieprawidłowa nazwa. Maksymalna ilość znaków: " + Choice.MAX_NAME_LENGTH).setEphemeral(true).queue();
            log.info("Length of name incorrect");
            return false;
        }
        if (optionId.getAsString().length() > Choice.MAX_NAME_LENGTH || optionId.getAsString().length() == 0) {
            event.reply("Nieprawidłowe id. Maksymalna ilość znaków: " + Choice.MAX_NAME_LENGTH).setEphemeral(true).queue();
            log.info("Length of id incorrect");
            return false;
        }
        if (DiscordBot.getJda().getRoleById(optionId.getAsString()) == null) {
            event.reply("Rola o podanym id nie istnieje!").setEphemeral(true).queue();
            log.info("Role by id={} is not exist", optionId.getAsString());
            return false;
        }
        List<Role> roles = roleRepository.findAll();
        if (roles.size() >= MAX_ROLES) {
            event.reply("Osiągnięto maksymalną liczbę ról które można zapisać").setEphemeral(true).queue();
            log.info("Max count of roles. Can not add new role ({})", roles.size());
            return false;
        }
        for (Role role : roles) {
            if (optionId.getAsString().equals(role.getDiscordId()) || optionName.getAsString().equals(role.getName())) {
                event.reply("Podana rola o id=" + optionId.getAsString() + " i nazwie=" + optionName.getAsString() +
                        "  już istnieje").setEphemeral(true).queue();
                log.info("Role is already exist {}", role);
                return false;
            }
        }
        return true;
    }

    public void removeRole(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping optionId = event.getOption(DISCORD_ROLE_OPTION_NAME_ID.getName());
        isOptionsValidToRemove(event, optionId);
        if (!isOptionsValidToRemove(event, optionId)) {
            return;
        }
        Optional<Role> roleOptional = findByDiscordRoleId(Objects.requireNonNull(optionId).getAsString());
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            deleteById(role.getId());
            event.reply("Usunięto rolę.").setEphemeral(true).queue();
            updateRoleList();
            return;
        }
        event.reply("Rola nie istnieje").setEphemeral(true).queue();
    }

    private boolean isOptionsValidToRemove(SlashCommandInteractionEvent event, OptionMapping optionId) {
        if (optionId == null) {
            event.reply("Wystąpił nieoczekiwany błąd. Skontaktuj się z <@642402714382237716>").queue();
            log.error("Null option");
            return false;
        }
        return true;
    }

    public void roleEvent(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping optionRole = event.getOption("role");
        if (optionRole == null) {
            return;
        }
        String roleName = optionRole.getAsString();
        List<Role> roles = findAll();
        for (Role role : roles) {
            if (role.getName().equals(roleName)) {
                String discordRoleId = role.getDiscordId();
                addRemoveRole(event, discordRoleId);
                return;
            }
        }
        event.reply("Error. Try again later or contact with Administrator").setEphemeral(true).queue();
    }

    public void addRemoveRole(SlashCommandInteractionEvent event, String roleId) {
        net.dv8tion.jda.api.entities.Role role = DiscordBot.getJda().getRoleById(roleId);
        Guild guild = RangersGuild.getGuild();
        if (guild == null) {
            event.reply("Error. Try again later or contact with Administrator").setEphemeral(true).queue();
            throw new NullPointerException("Null Guild");
        }
        if (role != null) {
            boolean hasRole = Users.hasUserRole(event.getUser().getId(), role.getId());
            if (!hasRole) {
                guild.addRoleToMember(UserSnowflake.fromId(event.getUser().getId()), role).queue();
                event.reply("**" + role.getName() + "** - Gave you the role!").queue();
                log.info(guild.getMemberById(event.getUser().getId()) + " - gave him role " + role.getName());
            } else {
                guild.removeRoleFromMember(UserSnowflake.fromId(event.getUser().getId()), role).queue();
                event.reply("**" + role.getName() + "** - Took away the role!").queue();
                log.info(guild.getMemberById(event.getUser().getId()) + " - take away the role " + role.getName());
            }
            return;
        }
        event.reply("Error. Try again later or contact with Administrator").setEphemeral(true).queue();
    }
}
