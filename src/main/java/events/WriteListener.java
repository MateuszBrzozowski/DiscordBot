package events;


import embed.*;
import helpers.Commands;
import helpers.IdRole;
import model.Recruits;
import model.SignUpMatch;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.List;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
            new Recruiter(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATYWNY)) {
            new EmbedNegative(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POZYTYWNY)) {
            new EmbedPositive(event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.ZAPISY)) {
            SignUpMatch matches = RangerBot.getMatches();
            matches.createSignUpList3Data(message, event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.CLOSE)) {
            Recruits recruits = RangerBot.getRecruits();
            recruits.closeChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.REOPEN)) {
            Recruits recruits = RangerBot.getRecruits();
            recruits.reOpenChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.HELPS)){
            new EmbedHelp(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.USUWANIE_KANALU)) {
            event.getMessage().delete().submit();
            logger.info("Usuwanie kanału.");
            Recruits recruits = RangerBot.getRecruits();
            recruits.deleteChannel(event);
        }
    }
}



