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
        builder.setTitle("PODANIE");
        builder.setDescription("Złóż podanie do klanu klikając przycisk poniżej");
        builder.addField("Wymagania", "- Wiek min. 16 lat\n" +
                "- Podstawowa umiejętność walki\n" +
                "- Podstawowa znajomość zasad rozgrywki w Squad\n" +
                "- Chęć gry zespołowej oraz wykonywania poleceń\n" +
                "- Teamspeak 3, słuchawki oraz mikrofon (dobry mikrofon!)\n" +
                "- Wolny czas na granie\n" +
                "- Przestrzeganie zasad \"Fair Play\" (żadnego używania cheatów, hacków, aim-botów i innych żałosnych pomocy psujących rozgrywkę)", false);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        event.getChannel().sendMessage(builder.build()).setActionRow(Button.secondary("newRecrut", "Podanie")/*,Button.primary("contact","Kontakt")*/).queue();
    }

}
