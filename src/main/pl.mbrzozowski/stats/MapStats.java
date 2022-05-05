package stats;

import embed.EmbedSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapStats {

    protected static final Logger logger = LoggerFactory.getLogger(Class.class.getName());
    private ArrayList<Maps> maps = new ArrayList<>();

    public void initialize() {
        pullAllMaps();
        calculatePercentage();
    }

    private void calculatePercentage() {
        float sumAllMaps = sumAllMaps();
        for (Maps m : maps) {
            float count = m.getCount();
            float percent = count * 100 / sumAllMaps;
            m.setPercentage(percent);
        }
    }

    private int sumAllMaps() {
        int sum = 0;
        for (Maps m : maps) {
            sum += m.getCount();
        }
        return sum;
    }

    private void pullAllMaps() {
        StatsDatabase database = new StatsDatabase();
        maps = database.pullAllMaps();
    }

    public void sendMapsStats(MessageReceivedEvent event) {
        DecimalFormat df = new DecimalFormat("0.00");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Maps");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        for (int i = 0; i < maps.size(); i++) {
            builder.addField(i + 1 + ". " + maps.get(i).getName(),
                    maps.get(i).getCount() + " - " + df.format(maps.get(i).getPercentage()) + "%",
                    false);
        }
        event.getTextChannel().sendMessageEmbeds(builder.build())
                .setActionRow(Button.primary("refreshMap", "Odśwież"))
                .queue();
    }
}
