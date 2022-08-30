package ranger.event;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.questionnaire.Questionnaires;
import ranger.Repository;

public class MessageUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        EventService e = Repository.getEvent();
        if (e.getIndexActiveEvent(event.getMessageId()) != -1) {
            e.cancelEvent(event.getMessageId());
        }
        Questionnaires q = Repository.getQuestionnaires();
        if (q.getIndex(event.getMessageId()) != -1) {
            q.removeQuestionnaire(event.getMessageId());
        }

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            Questionnaires questionnaires = Repository.getQuestionnaires();
            int questionnaireIndex = questionnaires.getIndex(event.getMessageId());
            if (questionnaireIndex != -1) {
                event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
                    try {
                        String emoji = event.getReaction().getReactionEmote().getEmoji();
                        if (!questionnaires.isPublic(questionnaireIndex)) {
                            event.getReaction().removeReaction(event.getUser()).queue();
                        } else {
                            if (!questionnaires.isMultiple(questionnaireIndex)) {
                                questionnaires.removeReaction(message, emoji, event.getUser());
                            }
                            if (!questionnaires.isCorrectReaction(questionnaireIndex, emoji)) {
                                event.getReaction().removeReaction(event.getUser()).queue();
                            }
                        }
                        questionnaires.saveAnswer(emoji, event.getMessageId(), event.getUser().getId());

                    } catch (IndexOutOfBoundsException e) {
                    } catch (IllegalStateException e) {
                        event.getReaction().removeReaction(event.getUser()).queue();
                    }
                });
            }
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.getUser().isBot()) {
            Questionnaires questionnaires = Repository.getQuestionnaires();
            int questionnaireIndex = questionnaires.getIndex(event.getMessageId());
            if (questionnaireIndex != -1) {
                if (questionnaires.isPublic(questionnaireIndex)) {
                    String emoji = event.getReaction().getReactionEmote().getEmoji();
                    questionnaires.removeAnswer(emoji, event.getMessageId(), event.getUser().getId());
                }
            }
        }
    }
}
