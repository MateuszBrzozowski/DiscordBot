package event;

import embed.EmbedSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import ranger.Repository;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventsSettings {

    private JDA jda = Repository.getJda();
    private Event event = Repository.getEvent();
    private final String userName;
    private final String userID;
    private List<String> eventID = new ArrayList<>();
    private final PrivateMessageReceivedEvent privateMsgEvent;
    private EventSettingsStatus stageOfSettings = EventSettingsStatus.CHOOSE_EVENT;

    public EventsSettings(PrivateMessageReceivedEvent privateEvent) {
        this.userID = privateEvent.getAuthor().getId();
        this.userName = privateEvent.getMessage().getAuthor().getName();
        this.privateMsgEvent = privateEvent;
        embedStart();
    }

    private void embedStart() {
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("MENADŻER EVENTÓW");
            builder.setColor(Color.YELLOW);
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.setDescription("Cześć " + userName.toUpperCase() + ".\n" +
                    "Wybierz event który chcesz edytować.\n\n" +
                    event.getActiveEventsIndexAndName());
            privateChannel.sendMessage(builder.build()).queue();
        });
    }

    public String getUserID() {
        return userID;
    }

    public void saveAnswerAndSetNextStage(PrivateMessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        switch (stageOfSettings) {
            case CHOOSE_EVENT: {

                break;
            }
        }
    }
}
