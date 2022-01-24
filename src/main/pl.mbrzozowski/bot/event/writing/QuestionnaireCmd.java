package bot.event.writing;

import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import questionnaire.Questionnaire;

public class QuestionnaireCmd extends Proccess {

    private GuildMessageReceivedEvent event;

    public QuestionnaireCmd(GuildMessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE)) {
            new Questionnaire(message.getContentDisplay(), message.getUserID(),event.getChannel().getId(), false);
        } else if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE_PUBLIC)) {
            new Questionnaire(message.getContentDisplay(), message.getUserID(), event.getChannel().getId(), true);
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
