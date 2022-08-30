package ranger.bot.events.writing;

import ranger.helpers.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ranger.questionnaire.Questionnaires;

public class QuestionnaireCmd extends Proccess {

    public QuestionnaireCmd(MessageReceivedEvent messageReceived) {
        super(messageReceived);
    }

    @Override
    public void proccessMessage(Message message) {
        if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE)) {
            Questionnaires.buildQuestionaire(message.getContentDisplay(), message.getUserID(), messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE_MULTIPLE)) {
            Questionnaires.buildQuestionaireMultiple(message.getContentDisplay(), message.getUserID(), messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length > 1 && message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE_PUBLIC)) {
            Questionnaires.buildQuestionairePublic(message.getContentDisplay(), message.getUserID(), messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length > 1 && (message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE_PUBLIC_MULTIPLE) || message.getWords()[0].equalsIgnoreCase(Commands.QUESTIONNAIRE_MULTIPLE_PUBLIC))) {
            Questionnaires.buildQuestionairePublicMultiple(message.getContentDisplay(), message.getUserID(), messageReceived.getChannel().getId());
            messageReceived.getMessage().delete().submit();
        } else if (message.getWords().length == 1 && isQuestionnaireCommand(message.getWords()[0])) {
            messageReceived.getChannel().sendMessage("Podaj pytanie do ankiety. np. ***!ankieta Czy lubisz lato?***").queue();
        } else {
            getNextProccess().proccessMessage(message);
        }
    }

    private boolean isQuestionnaireCommand(String word) {
        if (word.equalsIgnoreCase(Commands.QUESTIONNAIRE)) return true;
        if (word.equalsIgnoreCase(Commands.QUESTIONNAIRE_PUBLIC)) return true;
        if (word.equalsIgnoreCase(Commands.QUESTIONNAIRE_MULTIPLE)) return true;
        return word.equalsIgnoreCase(Commands.QUESTIONNAIRE_PUBLIC_MULTIPLE);
    }
}
