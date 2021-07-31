package bot.event.writing;

import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class EventsCmd extends Proccess {

    private GuildMessageReceivedEvent guildEvent;
    private PrivateMessageReceivedEvent privateEvent;

//    public Events(GuildMessageReceivedEvent guildEvent) {
//        this.guildEvent = guildEvent;
//    }
//
//    public Events(PrivateMessageReceivedEvent privateEvent) {
//        this.privateEvent = privateEvent;
//    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords()[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            if (privateEvent != null) {
                if (message.getWords().length == 4) {
                    getEvents().createNewEventFrom3Data(message.getWords(), privateEvent);
                } else if (message.getWords().length == 5) {
                    getEvents().createNewEventFrom4Data(message.getWords(), privateEvent);
                } else if (message.getWords().length >= 7) {
                    getEvents().createNewEventFromSpecificData(message.getWords(), privateEvent);
                }
            } else {
                guildEvent.getMessage().delete().submit();
                //TODO wyslac informacje ze komende uzywc tylko i wylaczie w prywatnej wiadomosci.lub obsluzyc,, no nie wiem.
            }
        } else if (message.getWords()[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            if (guildEvent != null) {
                guildEvent.getMessage().delete().submit();
                if (message.getWords().length == 4) {
                    getEvents().createNewEventFrom3DataHere(message.getWords(), guildEvent);
                } else if (message.getWords().length == 5) {
                    getEvents().createNewEventFrom4DataHere(message.getWords(), guildEvent);
                } else if (message.getWords().length >= 7) {
                    getEvents().createNewEventFromSpecificData(message.getWords(), guildEvent);
                }
            } else {
                //TODO wyslac informacje ze komende uzywac tylko na konkretnym kanale gdzie chcemy utworzyc zzapisy
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
