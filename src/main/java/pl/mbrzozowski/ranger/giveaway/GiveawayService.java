package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
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
        event.reply("Sprawdź wiadomości prywatne.").setEphemeral(true).queue();
        giveawayGenerator = new GiveawayGenerator(event.getUser());
    }

    public void selectAnswer(StringSelectInteractionEvent event) {
        giveawayGenerator.selectAnswer(event);
    }

    public void buttonGeneratorEvent(@NotNull ButtonInteractionEvent event) {
        if (giveawayGenerator == null) {
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
}
