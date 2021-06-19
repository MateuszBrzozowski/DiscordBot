package events;


import helpers.Commands;
import model.SignUpMatch;
import embed.EmbedNegative;
import embed.EmbedPositive;
import embed.EmbedRemoveChannel;
import embed.Recruiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class WriteListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split("\s+");

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
            new Recruiter(event);
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATYWNY)) {
            new EmbedNegative(event);
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POZYTYWNY)) {
            new EmbedPositive(event);
        } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.ZAPISY)) {
            //!zapisy nazwa data godzina
            event.getMessage().delete().complete();
            SignUpMatch match = new SignUpMatch();
            match.createSignUpList(message, event);
        } /*else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.USUWANIE_KANALU)) {

            new EmbedRemoveChannel(event);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.getGuild().getTextChannelById(event.getChannel().getId()).delete().reason("Rekrutacja zakończona").queue();
        }
        */
    }


}
