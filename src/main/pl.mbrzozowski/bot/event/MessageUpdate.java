package bot.event;

import event.Event;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import questionnaire.Questionnaires;
import ranger.Repository;

public class MessageUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        Event e = Repository.getEvent();
        if (e.getIndexActiveEvent(event.getMessageId()) != -1) {
            e.cancelEvent(event.getMessageId());
        }

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            Questionnaires questionnaires = Repository.getQuestionnaires();
            if (questionnaires.isQuestionnaire(event.getMessageId())) {
                event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
                    try {
                        String emoji = event.getReaction().getReactionEmote().getEmoji();
                        if (!questionnaires.isPublic(event.getMessageId())) {
                            event.getReaction().removeReaction(User.fromId(event.getUserId())).queue();
                        } else {
                            if (!questionnaires.isMultiple(event.getMessageId())) {
                                questionnaires.removeReaction(message, emoji, event.getUser());
                            }
                        }
                        questionnaires.saveAnswer(emoji, event.getMessageId(), event.getUser().getId());

                    } catch (IndexOutOfBoundsException e) {
                    } catch (IllegalStateException e) {
                        event.getReaction().removeReaction(User.fromId(event.getUser().getId())).queue();
                    }
                });
            }
        }

    }
}
