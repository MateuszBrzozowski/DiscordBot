package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        ArrayList<CommandData> commandData = new ArrayList<>();
        event.getGuild().updateCommands().queue();
//        if (event.getGuild().getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
//            ArrayList<CommandData> commandData = new ArrayList<>();
//            commandData.add(Commands.slash("welcome", "Przywitaj się mordo"));
//            event.getGuild().updateCommands().addCommands(commandData).queue();
//        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();
        if (name.equalsIgnoreCase("welcome")) {
            event.reply("Siemanenko").queue();
        }
    }
}
