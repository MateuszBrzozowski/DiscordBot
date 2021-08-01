package bot.event.writing;

import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ChannelCmd extends Proccess {

    private GuildMessageReceivedEvent receivedEvent;
    private PrivateMessageReceivedEvent privateEvent;

    public ChannelCmd(GuildMessageReceivedEvent receivedEvent) {
        this.receivedEvent = receivedEvent;
    }

    public ChannelCmd(PrivateMessageReceivedEvent privateEvent) {
        this.privateEvent = privateEvent;
    }

    @Override
    public void proccessMessage(Message message) {
        if (receivedEvent != null) {
            receivedEvent.getMessage().delete().submit();
            if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
                getEvents().createNewChannel(receivedEvent.getGuild(), receivedEvent.getAuthor().getId());
            } else if (message.getWords().length > 1 && message.getWords().length < 100 && message.getWords()[0].equalsIgnoreCase(Commands.NAME)){
                if (getEvents().checkChannelIsInEventCategory(receivedEvent)) {
                    String name = getNewChannelNameFromMsg(message.getWords());
                    receivedEvent.getMessage().delete().submit();
                    receivedEvent.getChannel().getManager().setName(name).queue();
                }
            }
        }
    }
//        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
//            if (receivedEvent != null) {
//                receivedEvent.getMessage().delete().submit();
//                getEvents().createNewChannel(receivedEvent.getGuild(), receivedEvent.getAuthor().getId());
//            }
//            if (getEvents().checkChannelIsInEventCategory(receivedEvent)) {
//                String newName = getNewChannelNameFromMsg(message.getWords());
//                receivedEvent.getChannel().getManager().setName(newName).queue();
//            }
//        }
//    }  else if(message.getWords().length >1&&message.getWords().length< 100&&message.getWords()[0].
//
//    equalsIgnoreCase(Commands.NAME))
//}

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
