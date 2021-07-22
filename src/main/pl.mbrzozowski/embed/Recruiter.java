package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

public class Recruiter extends ListenerAdapter {


    public Recruiter(GuildMessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("PODANIE");
        builder.addField("", "Chcemy nasze wieloletnie doświadczenie przekazać kolejnym Rangersom. Nasza gra opiera się na wzajemnej komunikacji i skoordynowanym działaniu. " +
                "Jako grupa, pielęgnujemy dobrą atmosferę i przyjazne, dojrzałe relacje między członkami naszego klanu, a także polską społecznością. \n", false);
//        builder.addBlankField(false);
        builder.addField("Złóż podanie do klanu klikając przycisk PONIŻEJ", "", false);
        builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
        builder.setFooter("*Złożenie podania powoduje otwarcie Twojej rekrutacji! ");
        event.getChannel().sendMessage(builder.build()).setActionRow(Button.success("newRecrut", "Podanie")).queue();
    }

}
