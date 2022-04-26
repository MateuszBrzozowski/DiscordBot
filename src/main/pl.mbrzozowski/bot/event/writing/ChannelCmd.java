package bot.event.writing;

import helpers.CategoryAndChannelID;
import helpers.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;

public class ChannelCmd extends Proccess {

    public ChannelCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
            String userID = message.getUserID();
            Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            if (messageReceived != null) {
                messageReceived.getMessage().delete().submit();
            }
            getEvents().createNewChannel(guild, userID);
        } else if (message.getWords().length > 1 && message.getWords().length < 100 && message.getWords()[0].equalsIgnoreCase(Commands.NAME)) {
            if (messageReceived != null) {
                if (getEvents().checkChannelIsInEventCategory(messageReceived)) {
                    String name = getNewChannelNameFromMsg(message.getWords());
                    messageReceived.getMessage().delete().submit();
                    messageReceived.getTextChannel().getManager().setName(name).queue();
                }
            }
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
            if (message.isAdmin()) {
                if (messageReceived != null) {
                    messageReceived.getMessage().delete().submit();
                    String channelID = messageReceived.getTextChannel().getId();
                    if (getRecruits().isRecruitChannel(channelID)) {
                        getRecruits().deleteChannel(messageReceived);
                    } else if (getEvents().isActiveMatchChannelID(channelID) >= 0) {
                        getEvents().deleteChannel(messageReceived);
                    }
                }
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    /**
     * @param message Wiadomość wpisana przez użytkownika
     * @return Zwraca nazwę wpisaną przez użytkownika.
     */
    private String getNewChannelNameFromMsg(String[] message) {
        String result = "";
        for (int i = 1; i < message.length; i++) {
            result += message[i] + " ";
        }
        return result;
    }
}
