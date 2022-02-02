package bot.event.writing;

import helpers.Commands;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import questionnaire.Questionnaires;

public class QuestionnaireCmd extends Proccess {

    private GuildMessageReceivedEvent event;

    public QuestionnaireCmd(GuildMessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE)) {
            Questionnaires.buildQuestionaire(message.getContentDisplay(), message.getUserID(), event.getChannel().getId());
            event.getMessage().delete().submit();
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
