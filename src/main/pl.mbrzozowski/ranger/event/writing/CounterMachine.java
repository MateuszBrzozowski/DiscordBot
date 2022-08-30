package ranger.event.writing;

import ranger.counter.Counter;
import ranger.helpers.Commands;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;

public class CounterMachine extends Proccess {

    public CounterMachine(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (!messageReceived.getAuthor().isBot() && !messageReceived.isFromType(ChannelType.PRIVATE)) {
            getCounter().userPlusOneMsg(message.getUserID());
        }
        Counter counter = Repository.getCounter();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TOP_THREE)) {
            counter.showTopThree(messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TOP_TEN)) {
            counter.showTopTen(messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.COUNT)) {
            counter.showUser(messageReceived.getAuthor().getId(), messageReceived.getChannel().getId());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
