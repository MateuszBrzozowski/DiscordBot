package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

public class Recruiter extends ListenerAdapter {


    public Recruiter(GuildMessageReceivedEvent event) {
        event.getMessage().delete().submit();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.addField("Podanie", "Złóż podanie do klanu klikając przycisk poniżej", false);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).setActionRow(Button.secondary("newRecrut", "Podanie")/*,Button.primary("contact","Kontakt")*/).queue();
    }

}
