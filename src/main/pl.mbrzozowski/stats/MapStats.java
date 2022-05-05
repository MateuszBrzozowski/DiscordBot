package stats;

import embed.EmbedSettings;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MapStats {

    protected static final Logger logger = LoggerFactory.getLogger(Class.class.getName());
    private ArrayList<Maps> maps = new ArrayList<>();
    private boolean isFresh = false;

    public void initialize() {
        refreshMapStats();
    }

    private void startTimerToClearFreshStatus() {
        Timer timer = new Timer();
        LocalDateTime time = LocalDateTime.now().plusHours(4);
        Date dateTimeNow = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
        timer.schedule(task(), dateTimeNow);
    }

    private TimerTask task() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isFresh) {
                    isFresh = false;
                    serachMessage(true);
                }
            }
        };
        return timerTask;
    }

    private void serachMessage(boolean changetToGreen) {
        JDA jda = Repository.getJda();
        TextChannel textChannel = jda.getTextChannelById(CategoryAndChannelID.CHANNEL_ADMIN_FAQ);
        textChannel.getHistory().retrievePast(100).queue(messages -> {
            checkMessages(messages, changetToGreen);
        });
    }

    private void changeButtonToRed(Message message) {
        List<MessageEmbed> embeds = message.getEmbeds();
        message.editMessageEmbeds(embeds.get(0))
                .setActionRow(Button.danger(Commands.REFRESH_MAP_STATS, "Odśwież")).queue();
    }

    private void changeButtonToGreen(Message message) {
        List<MessageEmbed> embeds = message.getEmbeds();
        message.editMessageEmbeds(embeds.get(0))
                .setActionRow(Button.success(Commands.REFRESH_MAP_STATS, "Odśwież")).queue();
    }

    private void checkMessages(List<Message> messages, boolean changetToGreen) {
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (!embeds.isEmpty()) {
                try {
                    String title = embeds.get(0).getTitle();
                    String footer = embeds.get(0).getFooter().getText().substring(0, 22);
                    if (title.equalsIgnoreCase("Maps") && footer.equalsIgnoreCase("Ostatnia aktualizacja:")) {
                        if (changetToGreen) {
                            changeButtonToGreen(messages.get(i));
                        } else {
                            changeButtonToRed(messages.get(i));
                            updateEmbeds(messages.get(i));
                        }
                    }
                } catch (NullPointerException exception) {
                }

            }
        }
    }

    private void updateEmbeds(Message message) {
        MessageEmbed mOld = message.getEmbeds().get(0);
        LocalDateTime dateTime = LocalDateTime.from(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")));
        String footerNewString = "Ostatnia aktualizacja: " + dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        MessageEmbed.Footer footerNew = new MessageEmbed.Footer(footerNewString, "", "");
        MessageEmbed m = new MessageEmbed(mOld.getUrl(),
                mOld.getTitle(),
                mOld.getDescription(),
                mOld.getType(),
                mOld.getTimestamp(),
                mOld.getColorRaw(),
                mOld.getThumbnail(),
                mOld.getSiteProvider(),
                mOld.getAuthor(),
                mOld.getVideoInfo(),
                footerNew,
                mOld.getImage(),
                getNewFields());
        message.editMessageEmbeds(m).queue();
    }

    private List<MessageEmbed.Field> getNewFields() {
        DecimalFormat df = new DecimalFormat("0.00");
        List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            MessageEmbed.Field field = new MessageEmbed.Field(i + 1 + ". " + maps.get(i).getName(),
                    maps.get(i).getCount() + " - " + df.format(maps.get(i).getPercentage()) + "%", false);
            fieldsNew.add(field);
        }
        return fieldsNew;
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
        calculatePercentage();
        isFresh = true;
    }

    void sendMapsStats(MessageReceivedEvent event) {
        pullAllMaps();
        DecimalFormat df = new DecimalFormat("0.00");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Maps");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        for (int i = 0; i < maps.size(); i++) {
            builder.addField(i + 1 + ". " + maps.get(i).getName(),
                    maps.get(i).getCount() + " - " + df.format(maps.get(i).getPercentage()) + "%",
                    false);
        }
        LocalDateTime dateTime = LocalDateTime.from(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")));
        builder.setFooter("Ostatnia aktualizacja: " + dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        event.getTextChannel().sendMessageEmbeds(builder.build())
                .setActionRow(Button.danger(Commands.REFRESH_MAP_STATS, "Odśwież"))
                .queue();
        startTimerToClearFreshStatus();
    }

    void refreshMapStats() {
        if (!isFresh) {
            pullAllMaps();
            serachMessage(false);
            startTimerToClearFreshStatus();
        }
    }
}
