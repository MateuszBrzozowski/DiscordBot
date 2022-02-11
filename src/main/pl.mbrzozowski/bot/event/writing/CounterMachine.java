package bot.event.writing;

import counter.Counter;
import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import ranger.Repository;

public class CounterMachine extends Proccess {

    private final GuildMessageReceivedEvent event;

    public CounterMachine(GuildMessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void proccessMessage(Message message) {
        if (!event.getAuthor().isBot()) {
            getCounter().userPlusOneMsg(message.getUserID());
        }
        Counter counter = Repository.getCounter();
        if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TOP_THREE)) {
            counter.showTopThree(event.getChannel().getId());
            event.getMessage().delete().submit();
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.TOP_TEN)) {
            counter.showTopTen(event.getChannel().getId());
            event.getMessage().delete().submit();
        } else if (message.getWords().length == 1 && message.getWords()[0].equalsIgnoreCase(Commands.COUNT)) {
            counter.showUser(event.getAuthor().getId(), event.getChannel().getId());
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
