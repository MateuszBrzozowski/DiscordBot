package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.helpers.Converter;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.response.EmbedSettings;

import java.awt.*;
import java.util.List;

@Slf4j
@Service
public class GiveawayService {

    private GiveawayGenerator giveawayGenerator;
    private final GiveawayRepository giveawayRepository;

    public GiveawayService(GiveawayRepository giveawayRepository) {
        this.giveawayRepository = giveawayRepository;
    }

    public void create(@NotNull SlashCommandInteractionEvent event) {
        if (giveawayGenerator == null) {
            event.reply("Sprawdź wiadomości prywatne.").setEphemeral(true).queue();
            giveawayGenerator = new GiveawayGenerator(event.getUser(), event.getChannel().asTextChannel(), this);
        } else {
            if (giveawayGenerator.userHasActiveGenerator(event.getUser())) {
                giveawayGenerator.cancel();
                event.reply("Sprawdź wiadomości prywatne").setEphemeral(true).queue();
                giveawayGenerator = new GiveawayGenerator(event.getUser(), event.getChannel().asTextChannel(), this);
            } else {
                event.reply("Inny użytkownik jest w czasie tworzenia giveawaya").setEphemeral(true).queue();
                log.info("{} - Cannot create new giveaway generator. {} has active generator", event.getUser(), giveawayGenerator.getUser());
            }
        }
    }

    public void selectAnswer(StringSelectInteractionEvent event) {
        if (giveawayGenerator == null) {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            return;
        }
        giveawayGenerator.selectAnswer(event);
    }

    public void buttonGeneratorEvent(@NotNull ButtonInteractionEvent event) {
        if (giveawayGenerator == null) {
            event.getMessage().delete().queue();
            return;
        }
        if (!giveawayGenerator.isActualActiveGenerator(event)) {
            event.getMessage().delete().queue();
            return;
        }
        if (event.getComponentId().equalsIgnoreCase(ComponentId.GIVEAWAY_GENERATOR_BTN_CANCEL)) {
            giveawayGenerator.cancel();
            giveawayGenerator = null;
        } else {
            giveawayGenerator.buttonEvent(event);
        }
    }

    public void generatorSaveAnswer(ModalInteractionEvent event) {
        giveawayGenerator.saveAnswer(event);
    }

    public void publishOnChannel(@NotNull TextChannel textChannel, @NotNull Giveaway giveaway, List<Prize> prizes) {
        if (prizes == null) {
            throw new NullPointerException("Prizes can not be null");
        }
        if (prizes.size() == 0) {
            throw new IllegalStateException("Prizes can not be empty");
        }
        giveaway.setPrizes(prizes);
        giveaway.setChannelId(textChannel.getId());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(143, 203, 209));
        builder.setDescription("## :tada:  GIVEAWAY  :tada:");
        builder.addField("Nagrody", GiveawayGenerator.getPrizesDescription(prizes), false);
        builder.addField(EmbedSettings.WHEN_END_DATE,
                Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveaway.getEndTime()) + "\n" +
                        EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(giveaway.getEndTime()),
                false);
        textChannel.sendMessageEmbeds(builder.build()).queue(message -> {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            message.editMessageEmbeds(messageEmbed)
                    .setActionRow(Button.success("giveaway" + message.getId() + "in", "Zapisz się"))
                    .queue();
            giveaway.setMessageId(message.getId());
            save(giveaway);
        });
    }

    private void save(Giveaway giveaway) {
        giveawayRepository.save(giveaway);
    }

    protected void setGiveawayGenerator(GiveawayGenerator giveawayGenerator) {
        this.giveawayGenerator = giveawayGenerator;
    }
}
