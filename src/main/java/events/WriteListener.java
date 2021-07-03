package events;


import embed.*;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import model.Event;
import model.Recruits;
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
        boolean radaKlanu = isRoleRadaKlanu(event);
        boolean clanMember = isRoleClanMember(event);
        Event matches = RangerBot.getMatches();

        event.getMessage().delete().submit();

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
            if (radaKlanu) new Recruiter(event);
        }
        else if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.RANGER_BOT_LOGGER)){
            new EmbedNoWriteOnLoggerChannel(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATIVE)) {
            if (radaKlanu) new EmbedNegative(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POSITIVE)) {
            if (radaKlanu) new EmbedPositive(event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom3Data(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom4Data(message, event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            matches.createNewEventFrom3DataHere(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            matches.createNewEventFrom4DataHere(message, event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)){
            matches.createNewEventFromSpecificData(message,event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)){
            matches.createNewEventFromSpecificData(message,event);
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
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
            event.getMessage().delete().submit();
            logger.info("Usuwanie kanału.");
            Recruits recruits = RangerBot.getRecruits();
            Event match = RangerBot.getMatches();
            String channelID = event.getChannel().getId();

            if (recruits.isRecruitChannel(channelID)){
                recruits.deleteChannel(event);
            }else{
                if (match.isActiveMatch(channelID)>=0){
                    match.deleteChannel(event);
                }
            }
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.DICE)){
            new EmbedDice(event);
        }
    }


    private boolean isRoleRadaKlanu(GuildMessageReceivedEvent event) {
        List<Role> roles = event.getMember().getRoles();
        for (Role r: roles){
            if (r.getId().equalsIgnoreCase(RoleID.RADA_KLANU)) {
                logger.info("true");
                return true;
            }
        }
        logger.info("false");
        return false;
    }

    private boolean isRoleClanMember(GuildMessageReceivedEvent event) {
        List<Role> roles = event.getMember().getRoles();
        for (Role r: roles){
            if (r.getId().equalsIgnoreCase(RoleID.CLAN_MEMBER_ID)) {
                logger.info("true");
                return true;
            }
        }
        logger.info("false");
        return false;
    }
}



