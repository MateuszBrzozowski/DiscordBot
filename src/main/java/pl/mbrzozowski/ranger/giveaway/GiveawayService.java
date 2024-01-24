package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.repository.main.GiveawayUsersRepository;
import pl.mbrzozowski.ranger.repository.main.PrizeRepository;

@Slf4j
@Service
public class GiveawayService {

    private final GiveawayRepository giveawayRepository;
    private final GiveawayUsersRepository giveawayUsersRepository;
    private final PrizeRepository prizeRepository;
    private GiveawayGenerator giveawayGenerator;

    public GiveawayService(GiveawayRepository giveawayRepository,
                           GiveawayUsersRepository giveawayUsersRepository,
                           PrizeRepository prizeRepository) {
        this.giveawayRepository = giveawayRepository;
        this.giveawayUsersRepository = giveawayUsersRepository;
        this.prizeRepository = prizeRepository;
    }

    public void create(@NotNull SlashCommandInteractionEvent event) {
        if (giveawayGenerator == null) {
            event.reply("Sprawdź wiadomości prywatne.").setEphemeral(true).queue();
            giveawayGenerator = new GiveawayGenerator(event.getUser());
        } else {
            if (giveawayGenerator.userHasActiveGenerator(event.getUser())) {
                giveawayGenerator.cancel();
                event.reply("Sprawdź wiadomości prywatne").setEphemeral(true).queue();
                giveawayGenerator = new GiveawayGenerator(event.getUser());
            } else {
                event.reply("Inny użytkownik jest w czasie tworzenia giveawaya").setEphemeral(true).queue();
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
}
