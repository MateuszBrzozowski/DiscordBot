package bot.event.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.Repository;

public class InvalidCmd extends Proccess {

    public InvalidCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        Repository.getJda().getUserById(message.getUserID()).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. \n" +
                    "**!generator** - otwiera generator eventów.\n" +
                    "**!events** - edytowanie aktywnych eventów.\n" +
                    "Jeżeli potrzebujesz więcej pomocy. Wpisz **!help**").queue();
        });
    }
}
