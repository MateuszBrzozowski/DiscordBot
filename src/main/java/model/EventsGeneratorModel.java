package model;

import embed.EventsGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventsGeneratorModel {
    private List<EventsGenerator> eventsGenerators = new ArrayList<>();

    public void addEventsGenerator(EventsGenerator eGM){
        eventsGenerators.add(eGM);
    }

    public int getSizeEventsGenerator(){
        return eventsGenerators.size();
    }

    public int userHaveActiveGenerator(String authorID) {
        if (!eventsGenerators.isEmpty()){
            for (int i=0; i<eventsGenerators.size() ; i++) {
                if (eventsGenerators.get(i).getUserID().equalsIgnoreCase(authorID)){
                    return i;
                }
            }
        }
        return -1;

    }

    public void saveAnswerAndNextStage(PrivateMessageReceivedEvent event, int indexOfGenerator) {
        eventsGenerators.get(indexOfGenerator).saveAnswerAndSetNextStage(event);
    }

    public void removeGenerator(int indexOfGenerator) {
        eventsGenerators.remove(indexOfGenerator);
    }

    public void cancelEventGenerator(PrivateMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("GENEROWANIE LISTY ZOSTAŁO PRZERWANE");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }

    public void cancelEventGenerator(GuildMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("GENEROWANIE LISTY ZOSTAŁO PRZERWANE");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                privateChannel.sendMessage(builder.build()).queue();
            });
        });
    }
}
