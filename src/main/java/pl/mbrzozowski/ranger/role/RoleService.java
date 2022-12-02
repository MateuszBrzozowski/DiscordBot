package pl.mbrzozowski.ranger.role;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.repository.main.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public boolean addRole(OptionMapping id, OptionMapping name, OptionMapping description) {
        if (id == null || name == null) {
            return false;
        }
        Role role = new Role(id.getAsString(), name.getAsString());
        if (description != null) {
            role.setDescription(description.getAsString());
        }
        save(role);
        return true;
    }

    public boolean removeRole(OptionMapping id) {
        if (id == null) {
            return false;
        }
        Optional<Role> roleOptional = findByDiscordRoleId(id.getAsString());
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            deleteById(role.getId());
            return true;
        }
        return false;
    }

    public SelectMenu getRoleToSelectMenu() {
        List<SelectOption> options = new ArrayList<>();
        List<Role> roleList = findAll();
        if (roleList.size() > 0) {
            for (Role role : roleList) {
                SelectOption option = SelectOption.of(role.getName(), role.getDiscordId());
                SelectOption selectOption = option.withDescription(role.getDescription());
                options.add(selectOption);
            }
        } else {
            options.add(SelectOption.of("NO ROLES", "NO ROLES"));
        }
        return StringSelectMenu.create(ComponentId.ROLES)
                .setPlaceholder("Choose a role")
                .setRequiredRange(1, 1)
                .addOptions(options)
                .build();
    }

    private void save(Role role) {
        roleRepository.save(role);
    }

    private void deleteByDiscordRoleId(String discordRoleId) {
        roleRepository.deleteByDiscordId(discordRoleId);
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
}
