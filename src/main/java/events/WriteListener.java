package events;


import embed.*;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import model.Event;
import model.Recruits;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        boolean radKlan = RoleID.isRoleMessageRecived(event,RoleID.RADA_KLANU);
        boolean clanMember = RoleID.isRoleMessageRecived(event,RoleID.CLAN_MEMBER_ID);
        Event matches = RangerBot.getMatches();
        Recruits recruits = RangerBot.getRecruits();

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
            event.getMessage().delete().submit();
            if (radKlan) new Recruiter(event);
        }
        else if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.RANGER_BOT_LOGGER)){
            new EmbedNoWriteOnLoggerChannel(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATIVE)) {
            event.getMessage().delete().submit();
            if (radKlan) new EmbedNegative(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POSITIVE)) {
            event.getMessage().delete().submit();
            if (radKlan) new EmbedPositive(event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom3Data(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom4Data(message, event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom3DataHere(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom4DataHere(message, event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)){
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFromSpecificData(message,event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)){
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFromSpecificData(message,event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.CLOSE)) {
            event.getMessage().delete().submit();
            if (radKlan) recruits.closeChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.REOPEN)) {
            event.getMessage().delete().submit();
            if (radKlan) recruits.reOpenChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.HELPS)){
            event.getMessage().delete().submit();
            if (radKlan) new EmbedHelp(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
            event.getMessage().delete().submit();
            if (radKlan) {
                logger.info("Usuwanie kanału.");
                String channelID = event.getChannel().getId();
                if (recruits.isRecruitChannel(channelID)){
                    recruits.deleteChannel(event);
                }else if (matches.isActiveMatchChannelID(channelID)>=0){
                    matches.deleteChannel(event);
                }
            }
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.DICE)){
            event.getMessage().delete().submit();
            new EmbedDice(event);
        }
    }

}



