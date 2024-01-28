package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
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
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;
import static pl.mbrzozowski.ranger.helpers.Constants.ZONE_ID_EUROPE_PARIS;

@Slf4j
@Service
public class GiveawayService {

    private GiveawayGenerator giveawayGenerator;
    private final GiveawayRepository giveawayRepository;

    public GiveawayService(GiveawayRepository giveawayRepository) {
        this.giveawayRepository = giveawayRepository;
        findActiveAndSetTimer();
    }

    private void findActiveAndSetTimer() {
        List<Giveaway> giveaways = giveawayRepository.findAll();
        giveaways.stream().filter(this::isActive).forEach(this::setTimerToEnding);
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

    void publishOnChannel(@NotNull TextChannel textChannel, @NotNull Giveaway giveaway, List<Prize> prizes) {
        validateGeneratorOutput(giveaway, prizes);
        giveaway.setPrizes(prizes);
        giveaway.setChannelId(textChannel.getId());
        EmbedBuilder builder = createEmbed(giveaway, prizes);
        sendEmbed(textChannel, giveaway, builder);

        setTimerToEnding(giveaway);
    }

    private void setTimerToEnding(@NotNull Giveaway giveaway) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                giveaway.getEndTime().getYear(),
                giveaway.getEndTime().getMonthValue(),
                giveaway.getEndTime().getDayOfMonth(),
                giveaway.getEndTime().getHour(),
                giveaway.getEndTime().getMinute(),
                giveaway.getEndTime().getSecond());
        timer.schedule(new EndGiveaway(this, giveaway), calendar.getTime());
    }


    private void validateGeneratorOutput(@NotNull Giveaway giveaway, List<Prize> prizes) {
        if (prizes == null) {
            throw new NullPointerException("Prizes can not be null");
        }
        if (prizes.size() == 0) {
            throw new IllegalStateException("Prizes can not be empty");
        }
        if (giveaway.getEndTime() == null) {
            throw new NullPointerException("End time can not be null");
        }
    }

    private void sendEmbed(@NotNull TextChannel textChannel, @NotNull Giveaway giveaway, @NotNull EmbedBuilder builder) {
        textChannel.sendMessageEmbeds(builder.build()).queue(message -> {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            message.editMessageEmbeds(messageEmbed)
                    .setActionRow(Button.success("giveawayIn" + message.getId(), "Zapisz się"))
                    .queue();
            giveaway.setMessageId(message.getId());
            save(giveaway);
        });
    }

    @NotNull
    private EmbedBuilder createEmbed(@NotNull Giveaway giveaway, List<Prize> prizes) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(143, 203, 209));
        builder.setDescription("## :tada:  GIVEAWAY  :tada:");
        builder.addField("Nagrody", GiveawayGenerator.getPrizesDescription(prizes), false);
        builder.addField(EmbedSettings.WHEN_END_DATE,
                Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveaway.getEndTime()) + "\n" +
                        EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(giveaway.getEndTime()),
                false);
        return builder;
    }

    private void save(Giveaway giveaway) {
        giveawayRepository.save(giveaway);
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event) {
        Giveaway giveaway = getGiveaway(event);
        boolean isActive = isActive(giveaway);
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        if (!isActive) {
            ResponseMessage.giveawayUnexpectedException(event);
            setEndEmbed(event.getMessage());
            throw new IllegalStateException(giveaway + " - Giveaway is ended. Button was still active");
        }
        if (isUserExist(event, giveaway)) {
            return;
        }
        saveUser(event, giveaway);
        updateEmbed(event, giveaway, messageEmbed);
        save(giveaway);
    }

    private boolean isUserExist(@NotNull ButtonInteractionEvent event, @NotNull Giveaway giveaway) {
        Optional<GiveawayUser> userOptional = giveaway.getGiveawayUsers()
                .stream()
                .filter(giveawayUser -> giveawayUser.getUserId().equalsIgnoreCase(event.getUser().getId()))
                .findFirst();
        if (userOptional.isPresent()) {
            ResponseMessage.giveawayUserExist(event);
            log.info("{}, {} is exist", event.getUser(), giveaway);
            return true;
        }
        return false;
    }

    private boolean isActive(@NotNull Giveaway giveaway) {
        return LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)).isBefore(giveaway.getEndTime());
    }

    @NotNull
    private Giveaway getGiveaway(@NotNull ButtonInteractionEvent event) {
        Optional<Giveaway> giveawayOptional = giveawayRepository.findByMessageId(event.getMessage().getId());
        if (giveawayOptional.isEmpty()) {
            ResponseMessage.giveawayUnexpectedException(event);
            throw new IllegalStateException("Giveaway not exist, messageId=" + event.getMessage().getId() + "");
        }
        Giveaway giveaway = giveawayOptional.get();
        if (giveaway.getEndTime() == null) {
            ResponseMessage.giveawayUnexpectedException(event);
            throw new NullPointerException("End time of giveaway is null.");
        }
        return giveaway;
    }

    void setEndEmbed(@NotNull Message message) {
        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder(messageEmbed);
        builder.setColor(new Color(151, 1, 95));
        message.editMessageEmbeds(builder.build()).setActionRow().queue();
        log.info("Embed set to end stage");
    }

    private void saveUser(@NotNull ButtonInteractionEvent event, Giveaway giveaway) {
        GiveawayUser giveawayUser = GiveawayUser.builder()
                .userId(event.getUser().getId())
                .userName(Users.getUserNicknameFromID(event.getUser().getId()))
                .timestamp(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)))
                .giveaway(giveaway)
                .build();
        giveaway.getGiveawayUsers().add(giveawayUser);
        ResponseMessage.giveawayAdded(event);
        log.info("{} added to giveaway {}", giveawayUser, giveaway);
    }

    private void updateEmbed(@NotNull ButtonInteractionEvent event, @NotNull Giveaway giveaway, MessageEmbed messageEmbed) {
        EmbedBuilder builder = new EmbedBuilder(messageEmbed);
        List<Field> fields = builder.getFields();
        if (giveaway.getGiveawayUsers().size() > 1) {
            fields.remove(1);
        }
        Field field = new Field("", "Liczba zapisanych: " + giveaway.getGiveawayUsers().size(), false);
        fields.add(1, field);
        event.getMessage().editMessageEmbeds(builder.build()).queue();

        log.info("Giveaway embed updated");
    }
}
