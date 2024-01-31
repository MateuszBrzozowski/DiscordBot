package pl.mbrzozowski.ranger.giveaway;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.*;
import pl.mbrzozowski.ranger.repository.main.GiveawayRepository;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;
import static pl.mbrzozowski.ranger.helpers.Constants.ZONE_ID_EUROPE_PARIS;

@Slf4j
@Service
public class GiveawayService {

    private GiveawayGenerator giveawayGenerator;
    private final GiveawayRepository giveawayRepository;
    private final Map<Long, Timer> timers = new HashMap<>();

    public GiveawayService(GiveawayRepository giveawayRepository) {
        this.giveawayRepository = giveawayRepository;
        findActiveAndSetTimer();
    }

    private void findActiveAndSetTimer() {
        List<Giveaway> giveaways = findByIsActive();
        giveaways.stream().filter(giveaway -> giveaway.getGiveawayUsers().size() > 0)
                .filter(giveaway -> {
                    List<GiveawayUser> giveawayUsers = giveaway.getGiveawayUsers().stream().filter(giveawayUser -> giveawayUser.getPrize() != null).toList();
                    return giveawayUsers.size() <= 0;
                }).forEach(this::setTimerToEnding);
    }

    private List<Giveaway> findByIsActive() {
        return giveawayRepository.findByIsActiveTrue();
    }

    @NotNull
    private List<Giveaway> findAll() {
        return giveawayRepository.findAll();
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
        giveawayGenerator.submit(event);
    }

    void publishOnChannel(@NotNull TextChannel textChannel, @NotNull GiveawayRequest giveawayRequest, List<Prize> prizes) {
        Giveaway giveaway = giveawayRequest.getGiveaway();
        validateGeneratorOutput(giveaway, prizes);
        for (Prize prize : prizes) {
            prize.setGiveaway(giveaway);
        }
        giveaway.setPrizes(prizes);
        giveaway.setChannelId(textChannel.getId());
        EmbedBuilder builder = createEmbed(giveaway, prizes);
        sendEmbed(textChannel, giveaway, builder, giveawayRequest.isMentionEveryone());
    }

    private void setTimerToEnding(@NotNull Giveaway giveaway) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(giveaway.getEndTime().getYear(), giveaway.getEndTime().getMonthValue() - 1, giveaway.getEndTime().getDayOfMonth(), giveaway.getEndTime().getHour(), giveaway.getEndTime().getMinute(), giveaway.getEndTime().getSecond());
        timer.schedule(new EndGiveaway(this, giveaway.getChannelId(), giveaway.getMessageId()), calendar.getTime());
        timers.put(giveaway.getId(), timer);
        log.info("Set timer to end giveaway{id={}}, date={}.{}.{} time={}:{}",
                giveaway.getId(),
                calendar.get(Calendar.DAY_OF_MONTH),
                String.format("%02d", calendar.get(Calendar.MONTH)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY),
                String.format("%02d", calendar.get(Calendar.MINUTE)));
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

    private void sendEmbed(@NotNull TextChannel textChannel, @NotNull Giveaway giveaway, @NotNull EmbedBuilder builder, boolean mentionEveryone) {
        if (mentionEveryone) {
            textChannel.sendMessage("@everyone").setEmbeds(builder.build()).queue(message -> {
                MessageEmbed messageEmbed = message.getEmbeds().get(0);
                message.editMessageEmbeds(messageEmbed).setActionRow(Button.success(ComponentId.GIVEAWAY_SIGN_IN + message.getId(), "Zapisz się")).queue();
                giveaway.setMessageId(message.getId());
                log.info("{}", giveaway);
                save(giveaway);
                setTimerToEnding(giveaway);
            });
        } else {
            textChannel.sendMessageEmbeds(builder.build()).queue(message -> {
                MessageEmbed messageEmbed = message.getEmbeds().get(0);
                message.editMessageEmbeds(messageEmbed).setActionRow(Button.success(ComponentId.GIVEAWAY_SIGN_IN + message.getId(), "Zapisz się")).queue();
                giveaway.setMessageId(message.getId());
                log.info("{}", giveaway);
                save(giveaway);
                setTimerToEnding(giveaway);
            });
        }
    }

    @NotNull
    private EmbedBuilder createEmbed(@NotNull Giveaway giveaway, List<Prize> prizes) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(143, 203, 209));
        builder.setDescription("## :tada:  GIVEAWAY  :tada:");
        builder.addField("Nagrody", GiveawayGenerator.getPrizesDescription(prizes), false);
        builder.addField("Koniec:", EmbedSettings.WHEN_END_DATE + Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveaway.getEndTime()) + "\n" + EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(giveaway.getEndTime()), false);
        return builder;
    }

    private void save(Giveaway giveaway) {
        giveawayRepository.save(giveaway);
    }

    public void buttonClickSignIn(@NotNull ButtonInteractionEvent event) {
        Giveaway giveaway = getGiveaway(event);
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        if (!isActive(giveaway)) {
            ResponseMessage.giveawayUnexpectedException(event);
            setEndEmbed(event.getMessage());
            throw new IllegalStateException(giveaway + " - Giveaway is ended. Button was still active");
        }
        if (isUserExist(event, giveaway)) {
            return;
        }
        if (isClanMemberExclude(event, giveaway)) {
            return;
        }
        saveUser(event, giveaway);
        updateEmbed(event, giveaway, messageEmbed);
        save(giveaway);
    }

    private boolean isClanMemberExclude(@NotNull ButtonInteractionEvent event, @NotNull Giveaway giveaway) {
        boolean isClanMember = Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID);
        if (giveaway.isClanMemberExclude() && isClanMember) {
            ResponseMessage.giveawayClanMemberExclude(event);
            log.info("{} is clan member. Giveaway{id={}} set exclude CM", event.getUser(), giveaway.getId());
            return true;
        }
        return false;
    }

    private boolean isUserExist(@NotNull ButtonInteractionEvent event, @NotNull Giveaway giveaway) {
        Optional<GiveawayUser> userOptional = giveaway.getGiveawayUsers().stream().filter(giveawayUser -> giveawayUser.getUserId().equalsIgnoreCase(event.getUser().getId())).findFirst();
        if (userOptional.isPresent()) {
            ResponseMessage.giveawayUserExist(event);
            log.info("{} is exist. GiveawayId={}", event.getUser(), giveaway.getId());
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
        Giveaway giveaway = findByMessageId(message.getId());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(151, 1, 95));
        builder.setDescription("## :tada:  GIVEAWAY  :tada:");
        builder.addField("", getWinnersString(giveaway), false);
        builder.addField("", "Zakończony: " + Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveaway.getEndTime()), false);
        message.editMessageEmbeds(builder.build()).setComponents().setSuppressEmbeds(false).queue();
        log.info("Embed set to end stage");
    }

    @NotNull
    private String getWinnersString(@NotNull Giveaway giveaway) {
        List<GiveawayUser> giveawayUsers = giveaway.getGiveawayUsers();
        List<GiveawayUser> winners = giveawayUsers.stream().filter(giveawayUser -> giveawayUser.getPrize() != null).toList();
        StringBuilder builder = new StringBuilder();
        if (winners.isEmpty()) {
            builder.append("Brak wygranych");
        } else {
            builder.append("Wygrani:\n");
        }
        for (GiveawayUser winner : winners) {
            builder.append("<@").append(winner.getUserId()).append("> - ").append(winner.getPrize().getName()).append("\n");
        }
        return builder.toString();
    }

    private void setEndEmbed(String channelId, String messageId) {
        JDA jda = DiscordBot.getJda();
        TextChannel textChannel = jda.getTextChannelById(channelId);
        if (textChannel != null) {
            textChannel.retrieveMessageById(messageId).queue(this::setEndEmbed);
        }
    }

    private void saveUser(@NotNull ButtonInteractionEvent event, Giveaway giveaway) {
        GiveawayUser giveawayUser = GiveawayUser.builder().userId(event.getUser().getId()).userName(Users.getUserNicknameFromID(event.getUser().getId())).timestamp(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS))).giveaway(giveaway).build();
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

    private void updateEmbed(@NotNull Giveaway giveaway) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            return;
        }
        textChannel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
            boolean active = isActive(giveaway);
            if (active) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(new Color(143, 203, 209));
                builder.setDescription("## :tada:  GIVEAWAY  :tada:");
                builder.addField("Nagrody", GiveawayGenerator.getPrizesDescription(giveaway.getPrizes()), false);
                if (giveaway.getGiveawayUsers().size() > 0) {
                    builder.addField("", "Liczba zapisanych: " + giveaway.getGiveawayUsers().size(), false);
                }
                builder.addBlankField(false);
                builder.addField(EmbedSettings.WHEN_END_DATE,
                        Converter.LocalDateTimeToTimestampDateTimeLongFormat(giveaway.getEndTime()) + "\n" +
                                EmbedSettings.WHEN_TIME + Converter.LocalDateTimeToTimestampRelativeFormat(giveaway.getEndTime()),
                        false);
                message.editMessageEmbeds(builder.build()).setSuppressEmbeds(false).queue();
            } else {
                setEndEmbed(message);
            }
            log.info("Giveaway embed fixed");
        });
    }

    public void draw(@NotNull String messageId) {
        log.info("Draw prizes for messageId={}", messageId);
        Giveaway giveaway = findByMessageId(messageId);
        log.info("Draw prizes for {}", giveaway);
        List<GiveawayUser> giveawayUsers = getGiveawayUsers(giveaway);
        List<Prize> prizes = getPrizes(giveaway);
        if (checkIsListsEmpty(giveawayUsers, prizes)) {
            return;
        }
        List<GiveawayUser> usersToDraw = new ArrayList<>(giveawayUsers);
        List<GiveawayUser> winUsers = new ArrayList<>();
        for (Prize prize : prizes) {
            for (int i = 0; i < prize.getNumberOfPrizes() && i <= usersToDraw.size(); i++) {
                log.info(String.valueOf(prize.getNumberOfPrizes()));
                log.info(String.valueOf(usersToDraw.size()));
                log.info("Draw {}", prize);
                if (usersToDraw.size() > 0) {
                    Random random = new Random();
                    int index = random.nextInt(usersToDraw.size());
                    GiveawayUser winUser = usersToDraw.get(index);
                    winUser.setPrize(prize);
                    winUsers.add(usersToDraw.get(index));
                    prize.getGiveawayUsers().add(usersToDraw.get(index));
                    usersToDraw.remove(index);
                }
                log.info(String.valueOf(prize.getNumberOfPrizes()));
                log.info(String.valueOf(usersToDraw.size()));
            }
        }
        setWinUsersToMainList(giveawayUsers, winUsers);
        giveaway.setActive(false);
        save(giveaway);
        log.info("Giveaway saved");
        sendInfoAboutWinners(giveaway);
    }

    private void setWinUsersToMainList(@NotNull List<GiveawayUser> giveawayUsers, @NotNull List<GiveawayUser> winUsers) {
        for (GiveawayUser winUser : winUsers) {
            for (GiveawayUser giveawayUser : giveawayUsers) {
                if (giveawayUser.getId().equals(winUser.getId())) {
                    giveawayUser.setPrize(winUser.getPrize());
                    log.info("Prize set for user {}", giveawayUser);
                }
            }
        }
    }

    private boolean checkIsListsEmpty(@NotNull List<GiveawayUser> giveawayUsers, List<Prize> prizes) {
        if (giveawayUsers.size() == 0) {
            log.info("Registered 0 users. Draw canceled");
            return true;
        }
        log.info("Registered {} users", giveawayUsers.size());
        if (prizes.size() == 0) {
            log.error("List of prizes is empty.");
            return true;
        }
        return false;
    }

    @NotNull
    private List<GiveawayUser> getGiveawayUsers(@NotNull Giveaway giveaway) {
        List<GiveawayUser> giveawayUsers = giveaway.getGiveawayUsers();
        if (giveawayUsers == null) {
            throw new IllegalStateException("List of users is null");
        }
        return giveawayUsers;
    }

    @NotNull
    private List<Prize> getPrizes(@NotNull Giveaway giveaway) {
        List<Prize> prizes = giveaway.getPrizes();
        if (prizes == null) {
            throw new IllegalStateException("List of prizes is null");
        }
        return prizes;
    }

    @NotNull
    private Giveaway findByMessageId(String messageId) {
        Optional<Giveaway> giveawayOptional = giveawayRepository.findByMessageId(messageId);
        if (giveawayOptional.isEmpty()) {
            throw new IllegalStateException("Giveaway not exist giveaway{messageId=" + messageId + "}");
        }
        return giveawayOptional.get();
    }

    public void cancel(@NotNull SlashCommandInteractionEvent event) {
        if (isFillId(event, false)) {
            return;
        }
        cancelEndResponse(event, false);
    }

    private void cancelEndResponse(@NotNull SlashCommandInteractionEvent event, boolean isEnd) {
        List<Giveaway> all = findAll();
        List<Giveaway> activeGiveaways = all.stream().filter(this::isActive).toList();
        if (activeGiveaways.size() == 0) {
            ResponseMessage.noGiveaways(event);
        } else if (activeGiveaways.size() == 1) {
            if (isEnd) {
                ResponseMessage.endGiveawayAreYouSure(event, activeGiveaways.get(0).getId().intValue());
            } else {
                ResponseMessage.cancelGiveawayAreYouSure(event, activeGiveaways.get(0).getId().intValue());
            }
        } else {
            ResponseMessage.moreThanOneGiveaway(event, activeGiveaways);
        }
    }

    public void end(@NotNull SlashCommandInteractionEvent event) {
        if (isFillId(event, true)) {
            return;
        }
        cancelEndResponse(event, true);
    }

    private boolean isFillId(@NotNull SlashCommandInteractionEvent event, boolean isEnd) {
        OptionMapping id = event.getOption(SlashCommands.GIVEAWAY_ID);
        if (id != null) {
            int idAsInt = id.getAsInt();
            Optional<Giveaway> giveawayOptional = findById(idAsInt);
            if (giveawayOptional.isEmpty()) {
                ResponseMessage.giveawayNoExist(event);
                log.info("Giveaway by id={} not exist in DB", idAsInt);
                return true;
            }
            Giveaway giveaway = giveawayOptional.get();
            boolean active = isActive(giveaway);
            if (active) {
                if (isEnd) {
                    ResponseMessage.endGiveawayAreYouSure(event, idAsInt);
                } else {
                    ResponseMessage.cancelGiveawayAreYouSure(event, idAsInt);
                }
            }
            return true;
        }
        return false;
    }

    private void cancelGiveaway(@NotNull Giveaway giveaway) {
        giveaway.setEndTime(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)));
        save(giveaway);
        setEndEmbed(giveaway.getChannelId(), giveaway.getMessageId());
        log.info("{} is canceled", giveaway);
    }

    private void endGiveaway(@NotNull Giveaway giveaway) {
        giveaway.setEndTime(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)));
        save(giveaway);
        setEndEmbed(giveaway.getChannelId(), giveaway.getMessageId());
        log.info("{} is ended", giveaway);
        draw(giveaway.getMessageId());
    }

    public void end(ButtonInteractionEvent event, String giveawayId, boolean isEnding) {
        Optional<Giveaway> giveawayOptional = findById(giveawayId);
        if (giveawayOptional.isEmpty()) {
            throw new IllegalArgumentException("Giveaway by id=" + giveawayId + " not exist in DB");
        }
        Giveaway giveaway = giveawayOptional.get();
        if (isActive(giveaway)) {
            giveaway.setActive(false);
            if (isEnding) {
                event.editMessage("Kończę i losuje nagrody dla giveawaya o id=" + giveawayId).setComponents().queue();
                endGiveaway(giveaway);
            } else {
                event.editMessage("Anuluje giveawaya o id=" + giveawayId).setComponents().queue();
                cancelGiveaway(giveaway);
            }
            timers.remove(giveaway.getId()).cancel();
        } else {
            ResponseMessage.giveawayEnded(event);
        }
    }

    @NotNull
    private Optional<Giveaway> findById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("ID is blank");
        }
        long lId = Long.parseLong(id);
        return findById(lId);
    }

    @NotNull
    private Optional<Giveaway> findById(int id) {
        Long lId = (long) id;
        return findById(lId);
    }

    @NotNull
    private Optional<Giveaway> findById(Long id) {
        return giveawayRepository.findById(id);
    }

    public void showActive(SlashCommandInteractionEvent event) {
        List<Giveaway> all = findAll();
        List<Giveaway> activeGiveaways = all.stream().filter(this::isActive).toList();
        if (activeGiveaways.size() > 0) {
            ResponseMessage.showActiveGiveaways(event, activeGiveaways);
        } else {
            ResponseMessage.noGiveaways(event);
        }
    }

    public void reRoll(@NotNull SlashCommandInteractionEvent event) {
        if (reRollIsFillId(event)) {
            return;
        }
        List<Giveaway> all = findAll();
        List<Giveaway> canReRoll = all.stream().filter(this::isCanReRoll).toList();
        if (canReRoll.size() == 0) {
            ResponseMessage.noGiveaways(event);
        } else if (canReRoll.size() == 1) {
            ResponseMessage.reRollAreYouSure(event, canReRoll.get(0).getId().intValue());
        } else {
            ResponseMessage.moreThanOneGiveaway(event, canReRoll);
        }
    }

    private boolean reRollIsFillId(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping id = event.getOption(SlashCommands.GIVEAWAY_ID);
        if (id != null) {
            int idAsInt = id.getAsInt();
            Optional<Giveaway> giveawayOptional = findById(idAsInt);
            if (giveawayOptional.isEmpty()) {
                ResponseMessage.giveawayNoExist(event);
                log.info("Giveaway by id={} not exist in DB", idAsInt);
                return true;
            }
            Giveaway giveaway = giveawayOptional.get();
            if (isCanReRoll(giveaway)) {
                ResponseMessage.reRollAreYouSure(event, idAsInt);
            }
            return true;
        }
        return false;
    }

    private boolean isCanReRoll(@NotNull Giveaway giveaway) {
        LocalDateTime endTime = giveaway.getEndTime();
        return endTime.isAfter(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)).minusDays(7)) &&
                endTime.isBefore(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPE_PARIS)));
    }

    public void reRoll(ButtonInteractionEvent event, String giveawayId) {
        Optional<Giveaway> giveawayOptional = findById(giveawayId);
        if (giveawayOptional.isEmpty()) {
            event.deferEdit().queue();
            throw new IllegalArgumentException("Giveaway by id=" + giveawayId + " not exist in DB");
        }
        Giveaway giveaway = giveawayOptional.get();
        if (isCanReRoll(giveaway)) {
            event.editMessage("Powtarzam losowanie").setComponents().queue();
            reRoll(giveaway);
        } else {
            ResponseMessage.giveawayNotPossibleReRoll(event);
        }
    }

    private void reRoll(Giveaway giveaway) {
        log.info("{} ReRoll prizes", giveaway);
        giveaway.getGiveawayUsers().forEach(giveawayUser -> giveawayUser.setPrize(null));
        giveaway.setActive(false);
        save(giveaway);
        log.info("Set null prizes for users in giveaway");
        draw(giveaway.getMessageId());
        setEndEmbed(giveaway.getChannelId(), giveaway.getMessageId());
    }

    private void sendInfoAboutWinners(@NotNull Giveaway giveaway) {
        List<Prize> prizes = giveaway.getPrizes();
        JDA jda = DiscordBot.getJda();
        TextChannel textChannel = jda.getTextChannelById(giveaway.getChannelId());
        if (textChannel == null) {
            return;
        }
        for (Prize prize : prizes) {
            for (int i = 0; i < prize.getGiveawayUsers().size(); i++) {
                textChannel.sendMessage("Gratulacje <@" + prize.getGiveawayUsers().get(i).getUserId() + ">! " +
                                "Wygrałeś **" + prize.getName() + "**!\n" + getLinkToGiveaway(giveaway))
                        .queue();
            }
        }
    }

    @NotNull
    private static String getLinkToGiveaway(@NotNull Giveaway giveaway) {
        return "https://discord.com/channels/" +
                CategoryAndChannelID.RANGERSPL_GUILD_ID +
                "/" +
                giveaway.getChannelId() +
                "/" +
                giveaway.getMessageId();
    }

    public void fixEmbed(@NotNull SlashCommandInteractionEvent event) {
        String messageId = Objects.requireNonNull(event.getOption("id")).getAsString();
        try {
            Giveaway giveaway = findByMessageId(messageId);
            event.reply("Naprawiam listę").setEphemeral(true).queue();
            updateEmbed(giveaway);

        } catch (IllegalStateException e) {
            event.reply("Event o podanym id nie istnieje").setEphemeral(true).queue();
        }
    }

    public void removeGenerator() {
        this.giveawayGenerator = null;
    }
}
