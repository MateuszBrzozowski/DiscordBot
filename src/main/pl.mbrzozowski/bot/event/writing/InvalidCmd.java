package bot.event.writing;

import ranger.Repository;

public class InvalidCmd extends Proccess {

    @Override
    public void proccessMessage(Message message) {
        Repository.getJda().getUserById(message.getUserID()).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. Jeżeli potrzebujesz pomocy. Wpisz **!help**").queue();
        });
    }
}
