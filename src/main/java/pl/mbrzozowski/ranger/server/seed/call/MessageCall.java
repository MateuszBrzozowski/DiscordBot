package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.settings.SettingsKey;
import pl.mbrzozowski.ranger.settings.SettingsService;
import pl.mbrzozowski.ranger.stats.model.PlayerCounts;

import java.util.*;

@Slf4j
public class MessageCall {

    public static final int MAX_PER_DAY = 4;
    private final static int MAX_CONDITIONS = 3;
    private final static int MAX_MESSAGES = 50;
    private final static int LENGTH_MESSAGE = 400;
    private final List<Conditions> conditions = new ArrayList<>();
    private final SettingsService settingsService;
    private final Levels level; //TODO czy ten level jest tu potrzebny?
    private final MessageService messageService;
    protected int messagePerDayCount = 0;
    protected int messagePerDay = 0;
    @Nullable
    private String roleId;

    protected MessageCall(SettingsService settingsService, MessageService messageService, Levels level) {
        this.messageService = messageService;
        this.settingsService = settingsService;
        this.level = level;
        pullMessagePerDayCount();
        pullMessagePerDay();
        pullConditions();
        pullRoleId();
        log.debug("MessageCall created. {}", this);
    }

    public int getMessagePerDay() {
        return messagePerDay;
    }

    public int getMessagePerDayCount() {
        return messagePerDayCount;
    }

    @Nullable
    public String getRoleId() {
        return roleId;
    }

    public List<Conditions> getConditions() {
        return conditions;
    }

    private void pullConditions() {
        if (level.equals(Levels.ONE)) {
            setConditions(SettingsKey.SEED_CALL_LEVEL_ONE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_ONE.getKey() + ".");
        } else if (level.equals(Levels.TWO)) {
            setConditions(SettingsKey.SEED_CALL_LEVEL_TWO_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_TWO.getKey() + ".");
        } else if (level.equals(Levels.THREE)) {
            setConditions(SettingsKey.SEED_CALL_LEVEL_THREE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_THREE.getKey() + ".");
        } else if (level.equals(Levels.FOUR)) {
            setConditions(SettingsKey.SEED_CALL_LEVEL_FOUR_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_FOUR.getKey() + ".");
        }
    }

    private void setConditions(SettingsKey settingsKey, String keyPrefix) {
        Optional<String> optional = settingsService.find(settingsKey);
        if (optional.isPresent()) {
            String conditionsCount = optional.get();
            if (!conditionsCount.chars().allMatch(Character::isDigit)) {
                log.warn("{} incorrect. Cancel load conditions.", settingsKey);
                return;
            }
            conditionsCount.chars().allMatch(Character::isDigit);
            int size = Integer.parseInt(optional.get());
            for (int i = 0; i < size; i++) {
                String keyPlayers = keyPrefix + i + ".players";
                String keyTime = keyPrefix + i + ".time";
                Optional<String> players = settingsService.find(keyPlayers);
                Optional<String> time = settingsService.find(keyTime);
                if (players.isEmpty() || time.isEmpty()) {
                    log.warn("Setting properties for key empty: {} or {}", keyPlayers, keyTime);
                    continue;
                }
                if (players.get().chars().allMatch(Character::isDigit) && time.get().chars().allMatch(Character::isDigit)) {
                    Conditions condition = new Conditions(Integer.parseInt(players.get()), Integer.parseInt(time.get()));
                    conditions.add(condition);
                    log.info("Settings properties loaded {}, {}", keyPlayers, keyTime);
                }
            }
        }
    }

    protected void addConditions(@NotNull SlashCommandInteractionEvent event) {
        if (conditions.size() >= MAX_CONDITIONS) {
            event.reply("Możesz ustawić maksymalnie 3 warunki.").setEphemeral(true).queue();
            return;
        }
        int players = Objects.requireNonNull(event.getOption("players")).getAsInt();
        int minutes = Objects.requireNonNull(event.getOption("minutes")).getAsInt();
        if (!new Analyzer().analyzeConditions(players, minutes)) {
            event.reply("- Ilość graczy musi być z przedziału od 1 do 100 włącznie\n" +
                    "- Minuty muszą być z przedziału od 1 do 120 włącznie.").setEphemeral(true).queue();
            return;
        }
        Conditions condition = new Conditions(players, minutes);
        this.conditions.add(condition);
        event.reply("Warunek dodany. Jeżeli " + condition.getPlayersCount() + " graczy przez " +
                condition.getWithinMinutes() + " minut").setEphemeral(true).queue();
        log.info("Add conditions {} for level: {}", condition, level.getLevel());
        saveConditions();
    }

    private void saveConditions() {
        if (level.equals(Levels.ONE)) {
            saveConditions(SettingsKey.SEED_CALL_LEVEL_ONE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_ONE.getKey() + ".");
        } else if (level.equals(Levels.TWO)) {
            saveConditions(SettingsKey.SEED_CALL_LEVEL_TWO_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_TWO.getKey() + ".");
        } else if (level.equals(Levels.THREE)) {
            saveConditions(SettingsKey.SEED_CALL_LEVEL_THREE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_THREE.getKey() + ".");
        } else if (level.equals(Levels.FOUR)) {
            saveConditions(SettingsKey.SEED_CALL_LEVEL_FOUR_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_FOUR.getKey() + ".");
        }
    }

    private void saveConditions(SettingsKey settingsKey, String keyPrefix) {
        settingsService.save(settingsKey, conditions.size());
        for (int i = 0; i < conditions.size(); i++) {
            String keyPlayers = keyPrefix + i + ".players";
            String keyTime = keyPrefix + i + ".time";
            settingsService.save(keyPlayers, conditions.get(i).getPlayersCount());
            settingsService.save(keyTime, conditions.get(i).getWithinMinutes());
        }
    }

    public void removeConditions(SlashCommandInteractionEvent event) {
        if (conditions.size() == 0) {
            event.reply("Brak warunków").setEphemeral(true).queue();
            return;
        }
        OptionMapping idOption = event.getOption("id");
        if (idOption == null) {
            if (conditions.size() == 1) {
                removeConditionsOnIndex(event, 0);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("**Więcej niż jeden warunek!** Wybierz ID. Wywołaj ponownie komendę z wybranym ID\n");
                for (int i = 1; i <= conditions.size(); i++) {
                    builder.append("ID: ").append(i).append(", Jeżeli ").append(conditions.get(i - 1).getPlayersCount())
                            .append(" graczy przez ").append(conditions.get(i - 1).getWithinMinutes()).append(" minut\n");
                }
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        } else {
            int index = idOption.getAsInt() - 1;
            if (index < 0 || index >= conditions.size()) {
                event.reply("Niepoprawne ID").setEphemeral(true).queue();
                return;
            }
            removeConditionsOnIndex(event, index);
        }
    }

    private void removeConditionsOnIndex(SlashCommandInteractionEvent event, int index) {
        removeAllConditionsFromSettings();
        Conditions remove = conditions.remove(index);
        saveConditions();
        replySuccessfully(event, remove);
    }

    void removeAllConditionsFromSettings() {
        String keyPrefix;
        switch (level) {
            case ONE -> keyPrefix = SettingsKey.SEED_CALL_LEVEL_ONE.getKey() + ".";
            case TWO -> keyPrefix = SettingsKey.SEED_CALL_LEVEL_TWO.getKey() + ".";
            case THREE -> keyPrefix = SettingsKey.SEED_CALL_LEVEL_THREE.getKey() + ".";
            case FOUR -> keyPrefix = SettingsKey.SEED_CALL_LEVEL_FOUR.getKey() + ".";
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
        for (int i = 0; i < conditions.size(); i++) {
            settingsService.deleteByKey(keyPrefix + i + ".time");
            settingsService.deleteByKey(keyPrefix + i + ".players");
        }
    }


    private void replySuccessfully(@NotNull SlashCommandInteractionEvent event, @NotNull Conditions conditions) {
        event.reply("Usunięto warunek: Jeżeli " + conditions.getPlayersCount() +
                        " graczy przez " + conditions.getWithinMinutes() + " minut")
                .setEphemeral(true)
                .queue();
        saveConditions();
        log.info("Remove conditions {} for level: {}", conditions, level);
    }

    private void pullRoleId() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role");
        optional.ifPresent(s -> this.roleId = s);
    }

    private List<Message> pullMessages() {
        return messageService.findByLevel(level);
    }

    protected void pullMessagePerDay() {
        SettingsKey key = getSettingsKeyMessagePerDay();
        Optional<String> optional = settingsService.find(key);
        if (optional.isEmpty()) {
            log.info("New settings property set {}={}", key, 0);
            settingsService.save(key, 0);
            return;
        }
        try {
            this.messagePerDay = Integer.parseInt(optional.get());
            if (messagePerDay < 0 || messagePerDay > MAX_PER_DAY) {
                throw new IllegalArgumentException("Message per day " + messagePerDay);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Settings property \"{}\" incorrect. Set default value={}", key, 0);
            settingsService.save(key, 0);
        }
    }

    @NotNull
    private SettingsKey getSettingsKeyMessagePerDay() {
        SettingsKey key;
        switch (level) {
            case ONE -> key = SettingsKey.SEED_CALL_LEVEL_ONE;
            case TWO -> key = SettingsKey.SEED_CALL_LEVEL_TWO;
            case THREE -> key = SettingsKey.SEED_CALL_LEVEL_THREE;
            case FOUR -> key = SettingsKey.SEED_CALL_LEVEL_FOUR;
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
        return key;
    }

    private void pullMessagePerDayCount() {
        SettingsKey key = getSettingsKeyMessagePerDayCount();
        Optional<String> optional = settingsService.find(key);
        if (optional.isEmpty()) {
            settingsService.save(key, 0);
            this.messagePerDayCount = 0;
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            settingsService.save(key, 0);
            this.messagePerDayCount = 0;
            return;
        }
        this.messagePerDayCount = Integer.parseInt(optional.get());
    }

    @NotNull
    private SettingsKey getSettingsKeyMessagePerDayCount() {
        SettingsKey key;
        switch (level) {
            case ONE -> key = SettingsKey.SEED_CALL_LEVEL_ONE_COUNT;
            case TWO -> key = SettingsKey.SEED_CALL_LEVEL_TWO_COUNT;
            case THREE -> key = SettingsKey.SEED_CALL_LEVEL_THREE_COUNT;
            case FOUR -> key = SettingsKey.SEED_CALL_LEVEL_FOUR_COUNT;
            default -> throw new UnsupportedOperationException(String.valueOf(level));
        }
        return key;
    }


    void setMaxAmount(@NotNull SlashCommandInteractionEvent event) {
        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 0 || count > MAX_PER_DAY) {
            event.reply("Niepoprawna wartość! Maksymalna - " + MAX_PER_DAY).setEphemeral(true).queue();
            log.error("Option incorrect - {}", count);
            return;
        }
        messagePerDay = count;
        settingsService.save(getSettingsKeyMessagePerDay(), count);
        event.reply("Ustawiono maksymalną ilość wiadomości - " + count).setEphemeral(true).queue();
        log.info("Set max amount {} for level: {}", count, level.getLevel());
    }

    public String getConditionsAsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("**Warunki dla levelu ").append(level.getLevel()).append(":**\n");
        if (conditions.size() == 0) {
            builder.append("> Brak warunków\n");
            return builder.toString();
        }
        for (Conditions condition : conditions) {
            builder.append("- Jeżeli ").append(condition.getPlayersCount()).append(" graczy przez ").append(condition.getWithinMinutes())
                    .append(" minut\n");
        }
        return builder.toString();
    }

    public int getConditionsSize() {
        return conditions.size();
    }

    public boolean analyzeConditions(List<PlayerCounts> players) {
        return new Analyzer().analyzeConditionsWithPlayerCount(players, conditions);
    }

    public boolean analyzeConditionsWhileStart(List<PlayerCounts> players) {
        return new Analyzer().analyzeConditionsWhileStart(players, conditions);
    }

    public void addMessagePerDayCount() {
        this.messagePerDayCount++;
        if (level.equals(Levels.ONE)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_ONE_COUNT, this.messagePerDayCount);
        } else if (level.equals(Levels.TWO)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_TWO_COUNT, this.messagePerDayCount);
        } else if (level.equals(Levels.THREE)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_THREE_COUNT, this.messagePerDayCount);
        } else if (level.equals(Levels.FOUR)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_FOUR_COUNT, this.messagePerDayCount);
        }
    }

    public void sendMessage(@NotNull List<PlayerCounts> players, String channelId) {
        if (messagePerDayCount >= messagePerDay) {
            log.info("Max message per day on this level");
            return;
        }
        TextChannel textChannel = RangersGuild.getTextChannel(channelId);
        if (textChannel == null) {
            log.warn("Channel not found");
            return;
        }
        StringBuilder builder = new StringBuilder();
        MessageModifier.addRole(builder, roleId);
        builder.append(" ");
        players.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        int currentPlayerCount = players.get(0).getPlayers();
        String message = getRandomMessage();
        MessageModifier.addPLayersCount(builder, message, currentPlayerCount);
        addMessagePerDayCount();
        textChannel.sendMessage(builder.toString()).queue();
        log.info("Sent seed call message");
    }

    private String getRandomMessage() {
        List<Message> messages = pullMessages();
        Random random = new Random();
        int nextInt = random.nextInt(messages.size());
        return messages.get(nextInt).getMessage();
    }

    public void resetMessageCount() {
        messagePerDayCount = 0;
        if (level.equals(Levels.ONE)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_ONE_COUNT, 0);
        } else if (level.equals(Levels.TWO)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_TWO_COUNT, 0);
        } else if (level.equals(Levels.THREE)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_THREE_COUNT, 0);
        } else if (level.equals(Levels.FOUR)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_FOUR_COUNT, 0);
        }
    }

    @Override
    public String toString() {
        return "MessageCall{" +
                "conditions=" + conditions +
                ", type=" + level +
                ", MAX_PER_DAY=" + MAX_PER_DAY +
                ", messagePerDayCount=" + messagePerDayCount +
                ", messagePerDay=" + messagePerDay +
                '}';
    }

    public void addMessage(SlashCommandInteractionEvent event) {
        List<Message> messages = pullMessages();
        if (messages.size() >= MAX_MESSAGES) {
            log.info("No space to new message");
            event.reply("Osiągnięto maksymalną ilość wiadomości").setEphemeral(true).queue();
            return;
        }
        OptionMapping messageOption = event.getOption("wiadomość");
        if (messageOption == null) {
            log.error("Null message");
            event.reply("Wystąpił nieoczekiwany błąd.").setEphemeral(true).queue();
            return;
        }
        String message = messageOption.getAsString();
        if (message.length() > LENGTH_MESSAGE || message.length() == 0) {
            log.info("Message empty or to long");
            event.reply("Zbyt długa wiadomości. Maksymalna ilość znaków: " + LENGTH_MESSAGE).setEphemeral(true).queue();
            return;
        }
        Message newMessage = new Message(null, message, level);
        event.reply("**Wiadomość dodana.**\n" + message).setEphemeral(true).queue();
        log.info("Message added");
        messageService.save(newMessage);
    }

    public void removeMessage(SlashCommandInteractionEvent event) {
        List<Message> messages = pullMessages();
        if (messages.size() == 0) {
            event.reply("Brak wiadomości na tym levelu").setEphemeral(true).queue();
            return;
        }
        OptionMapping idOption = event.getOption("id");
        if (idOption == null) {
            if (messages.size() == 1) {
                messages.clear();
                messageService.deleteByLevel(level);
                event.reply("Wiadomość usunięta. Brak więcej wiadomości na tym levelu").setEphemeral(true).queue();
            } else {
                event.reply("""
                        **Więcej niż jedna wiadomość na tym levelu.**
                        - Sprawdź id przy pomocy komendy /seed-call-wiadomości
                        - Wywołaj ponownie to polecenie wpisując wybrane id""").setEphemeral(true).queue();
            }
        } else {
            long id = idOption.getAsLong();
            boolean isRemoved = messages.removeIf(message -> message.getId().equals(id));
            if (isRemoved) {
                messageService.deleteById(id);
                event.reply("Wiadomość usunięta.").setEphemeral(true).queue();
            } else {
                event.reply("Brak wiadomości o tym id na tym levelu.").setEphemeral(true).queue();
            }
        }
    }

    public void showAllMessages(SlashCommandInteractionEvent event) {
        List<Message> messages = pullMessages();
        if (messages.size() == 0) {
            event.reply("Brak wiadomości na tym levelu").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(getMessagesAsDescription(messages));
        if (roleId != null && DiscordBot.getJda().getRoleById(roleId) != null) {
            Role role = DiscordBot.getJda().getRoleById(roleId);
            if (role != null) {
                builder.addField("Pinguj role:", role.getAsMention(), false);
            }
        }
        builder.addField("", getConditionsAsString(), false);
        builder.addField("Ilość wiadomości:", String.valueOf(this.messagePerDay), false);
        if (messages.size() > 10) {
            builder.setFooter("Strona 1");
            event.replyEmbeds(builder.build()).setComponents(ActionRow.of(getButtons(messages))).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(builder.build()).setComponents().setEphemeral(true).queue();
    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons(@NotNull List<Message> messages) {
        List<Button> buttons = new ArrayList<>();
        if (messages.size() > 10) {
            buttons.add(Button.primary(ComponentId.SEED_CALL_BACK, "⮜"));
            buttons.add(Button.primary(ComponentId.SEED_CALL_NEXT, "⮞"));
        }
        return buttons;
    }

    @NotNull
    private String getMessagesAsDescription(@NotNull List<Message> messages) {
        StringBuilder builder = new StringBuilder("## Wiadomości na levelu ").append(level.getLevel()).append("\n");
        for (int i = 0; i < messages.size() && i < 10; i++) {
            builder.append("ID: ").append(messages.get(i).getId()).append(" - ").append(messages.get(i).getMessage()).append("\n");
        }
        return builder.toString();
    }

    public int getMessageSize() {
        List<Message> messages = pullMessages();
        return messages.size();
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event) {
        List<Message> messages = pullMessages();
        String pageAsString = Objects.requireNonNull(event.getMessage().getEmbeds().get(0).getFooter()).getText();
        pageAsString = Objects.requireNonNull(pageAsString).substring("Strona ".length(), "Strona ".length() + 1);
        int page = Integer.parseInt(pageAsString);
        EmbedBuilder builder = new EmbedBuilder();
        if (event.getComponentId().equals(ComponentId.SEED_CALL_NEXT)) {
            if (page == 5 || messages.size() < page * 10) {
                return;
            } else {
                page++;
            }

        } else if (event.getComponentId().equals(ComponentId.SEED_CALL_BACK)) {
            if (page == 1) {
                return;
            } else {
                page--;
            }
        }
        builder.setDescription(getMessagesAsDescription(messages, page));
        if (roleId != null && DiscordBot.getJda().getRoleById(roleId) != null) {
            Role role = DiscordBot.getJda().getRoleById(roleId);
            if (role != null) {
                builder.addField("Pinguj role:", role.getAsMention(), false);
            }
        }
        builder.addField("", getConditionsAsString(), false);
        builder.addField("Ilość wiadomości:", String.valueOf(this.messagePerDay), false);
        builder.setFooter("Strona " + page);
        event.getMessage().editMessageEmbeds(builder.build()).queue();
    }

    @NotNull
    private String getMessagesAsDescription(@NotNull List<Message> messages, int page) {
        StringBuilder builder = new StringBuilder("## Wiadomości na levelu ").append(level.getLevel()).append("\n");
        for (int i = (page - 1) * 10; i < messages.size() && i < (page - 1) * 10 + 10; i++) {
            builder.append("ID: ").append(messages.get(i).getId()).append(" - ").append(messages.get(i).getMessage()).append("\n");
        }
        return builder.toString();
    }

    public void saveRoleId(String roleId) {
        removeRoleIdFromSettings();
        this.roleId = roleId;
        if (this.roleId != null) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role", roleId);
        }
    }

    public void removeRoleIdFromSettings() {
        settingsService.deleteByKey(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role");
    }

    public void deleteRole() {
        this.roleId = null;
        settingsService.deleteByKey(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role");
    }

    public void setMessagesToTempLevel() {
        List<Message> messages = messageService.findByLevel(level);
        for (Message message : messages) {
            message.setLevel(Levels.TEMP);
        }
        messageService.saveAll(messages);
    }

    public void applyNew(@NotNull MessageCall messageCall) {
        applyNewMessagePerDay(messageCall.getMessagePerDay());
        applyNewMessagePerDayCount(messageCall.getMessagePerDayCount());
        saveRoleId(messageCall.getRoleId());
        applyNewConditions(messageCall.getConditions());
        List<Message> messages = messageCall.pullMessages();
        applyNewMessages(messages);
    }

    public void applyNewMessagePerDay(int messagePerDay) {
        this.messagePerDay = messagePerDay;
        SettingsKey keyMessagePerDay = getSettingsKeyMessagePerDay();
        settingsService.save(keyMessagePerDay, this.messagePerDay);
    }

    public void applyNewMessagePerDayCount(int messagePerDayCount) {
        this.messagePerDayCount = messagePerDayCount;
        SettingsKey keyMessagePerDayCount = getSettingsKeyMessagePerDayCount();
        settingsService.save(keyMessagePerDayCount, this.messagePerDayCount);
    }


    public void applyNewConditions(List<Conditions> conditions) {
        this.conditions.clear();
        this.conditions.addAll(conditions);
        saveConditions();
    }

    public void applyNewMessages(@NotNull List<Message> messages) {
        for (Message message : messages) {
            message.setLevel(level);
        }
        messageService.saveAll(messages);
    }

    public void applyNewFromTemp(@NotNull MessageCall messageCall) {
        applyNewMessagePerDay(messageCall.getMessagePerDay());
        applyNewMessagePerDayCount(messageCall.getMessagePerDayCount());
        applyNewConditions(messageCall.getConditions());
        saveRoleId(messageCall.getRoleId());
        List<Message> messages = messageService.findByLevel(Levels.TEMP);
        applyNewMessages(messages);
    }
}
