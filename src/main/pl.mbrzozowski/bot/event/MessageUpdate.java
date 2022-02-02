package bot.event;

import event.Event;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

    private final String QUESTIONNAIRE = "Ankieta";

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
            event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
                try {
                    MessageEmbed messageEmbed = message.getEmbeds().get(0);
                    String title = messageEmbed.getTitle();
                    if (title.equalsIgnoreCase(QUESTIONNAIRE)) {
                        String emoji = event.getReaction().getReactionEmote().getEmoji();
                        Questionnaires questionnaires = Repository.getQuestionnaires();
                        questionnaires.saveAnswer(emoji, event.getMessageId(), event.getUser().getId());
                        event.getReaction().removeReaction(User.fromId(event.getUser().getId())).queue();
                    }
                } catch (IndexOutOfBoundsException e) {
                } catch (IllegalStateException e) {
                    event.getReaction().removeReaction(User.fromId(event.getUser().getId())).queue();
                }
            });
        }

    }
}
