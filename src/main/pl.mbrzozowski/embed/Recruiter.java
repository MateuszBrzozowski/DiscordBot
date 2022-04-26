package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class Recruiter {

    public Recruiter(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("PODANIE");
        builder.addField("", "Chcemy nasze wieloletnie doświadczenie przekazać kolejnym Rangersom. Nasza gra opiera się na wzajemnej komunikacji i skoordynowanym działaniu. " +
                "Jako grupa, pielęgnujemy dobrą atmosferę i przyjazne, dojrzałe relacje między członkami naszego klanu, a także polską społecznością. \n", false);
        builder.addField("Złóż podanie do klanu klikając przycisk PONIŻEJ", "", false);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(Button.success("newRecrut", "Podanie")).queue();
    }
}
