package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.DiscordBot;

public class InvalidCmd extends Proccess {

    public InvalidCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        DiscordBot.getJda().getUserById(message.getUserID()).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. \n" +
                    "**!event** - otwiera generator eventów.\n" +
                    "**!eventsEdit** - edytowanie aktywnych eventów.\n" +
                    "Jeżeli potrzebujesz więcej pomocy. Wpisz **!help**").queue();
        });
    }
}
