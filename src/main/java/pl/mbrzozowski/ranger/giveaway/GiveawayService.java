package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.ComponentId;

import java.util.List;

@Slf4j
@Service
public class GiveawayService {

    private GiveawayGenerator giveawayGenerator;

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

    public void publishOnChannel(TextChannel textChannel, Giveaway giveaway, List<Prize> prizes) {
        log.info("Tworze Giveawaya na kanale ");
        log.info("Na kanale {}", textChannel);
        log.info("{}", giveaway);
        log.info("{}", prizes);
    }

    protected void setGiveawayGenerator(GiveawayGenerator giveawayGenerator) {
        this.giveawayGenerator = giveawayGenerator;
    }
}
