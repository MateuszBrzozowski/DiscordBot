package pl.mbrzozowski.ranger.model;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

public interface SlashCommand {

    void getCommandsList(ArrayList<CommandData> commandData);
}
