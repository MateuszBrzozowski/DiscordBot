package bot.event.writing;

import helpers.CategoryAndChannelID;
import helpers.Commands;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import ranger.Repository;

public class ChannelCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;
    private PrivateMessageReceivedEvent privateEvent;

    public ChannelCmd(GuildMessageReceivedEvent receivedEvent) {
        this.guildEvent = receivedEvent;
    }

    public ChannelCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
            String userID;
            Guild guild = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            if (guildEvent != null) {
                guildEvent.getMessage().delete().submit();
                userID = guildEvent.getAuthor().getId();
            } else {
                userID = privateEvent.getAuthor().getId();
            }
            getEvents().createNewChannel(guild, userID);
        } else if (message.getWords().length > 1 && message.getWords().length < 100 && message.getWords()[0].equalsIgnoreCase(Commands.NAME)) {
            if (guildEvent != null) {
                if (getEvents().checkChannelIsInEventCategory(guildEvent)) {
                    String name = getNewChannelNameFromMsg(message.getWords());
                    guildEvent.getMessage().delete().submit();
                    guildEvent.getChannel().getManager().setName(name).queue();
                }
            }
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
            if (message.isAdmin()) {
                if (guildEvent != null) {
                    guildEvent.getMessage().delete().submit();
                    String channelID = guildEvent.getChannel().getId();
                    if (getRecruits().isRecruitChannel(channelID)) {
                        getRecruits().deleteChannel(guildEvent);
                    } else if (getEvents().isActiveMatchChannelID(channelID) >= 0) {
                        getEvents().deleteChannel(guildEvent);
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
