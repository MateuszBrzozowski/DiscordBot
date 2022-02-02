package bot.event;

import event.Event;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
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
        //TODO tylko jeśli uzytkownik a nie bot dodaje reakcje
//        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
//            try {
//                MessageEmbed messageEmbed = message.getEmbeds().get(0);
//                String title = messageEmbed.getTitle();
//                if (title.equalsIgnoreCase("Ankieta")) {
//                    MessageReaction.ReactionEmote reactionEmote = event.getReaction().getReactionEmote();
//
//                    logger.info(String.valueOf(reactionEmote.getEmoji().equalsIgnoreCase("\uD83C\uDDE8")));
//
//                    logger.info("to jest ankieta");
//                    Questionnaires questionnaires = Repository.getQuestionnaires();
//                    questionnaires.saveAnswer(reactionEmote.getEmoji(),event.getMessageId(),event.getUser().getId());
//                }
//            } catch (IndexOutOfBoundsException e) {
//            }
//        });
    }
}
