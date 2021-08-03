package bot.event.writing;

import helpers.RangerLogger;
import helpers.Users;
import ranger.Repository;

public class InvalidCmd extends Proccess{

    @Override
    public void proccessMessage(Message message) {
//        RangerLogger.info(Users.getUserNicknameFromID(message.getUserID()) + " - Nieprawidłowa komenda: [" + message.getContentDisplay() + "]");
        Repository.getJda().getUserById(message.getUserID()).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. Jeżeli potrzebujesz pomocy. Wpisz **!help**").queue();
        });
    }
}
