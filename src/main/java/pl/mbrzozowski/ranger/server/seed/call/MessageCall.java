package pl.mbrzozowski.ranger.server.seed.call;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.ComponentId;
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
    private final SettingsKey settingsKeyPerDay;
    private final Levels level;
    private final MessageService messageService;
    protected List<Message> messages = new ArrayList<>();
    protected int messagePerDayCount = 0;
    protected int messagePerDay = 0;
    @Nullable
    private String roleId;

    protected MessageCall(SettingsService settingsService, MessageService messageService, SettingsKey settingsKeyPerDay, Levels level) {
        this.messageService = messageService;
        this.settingsService = settingsService;
        this.settingsKeyPerDay = settingsKeyPerDay;
        this.level = level;
        pullMessagePerDayCount();
        pullMessagePerDay();
        pullConditions();
        pullMessages();
        pullRoleId();
        log.debug("MessageCall created. {}", this);
    }

    public int getMessagePerDay() {
        return messagePerDay;
    }

    public int getMessagePerDayCount() {
        return messagePerDayCount;
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
        saveSettings();
    }

    private void saveSettings() {
        if (level.equals(Levels.ONE)) {
            saveSettings(SettingsKey.SEED_CALL_LEVEL_ONE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_ONE.getKey() + ".");
        } else if (level.equals(Levels.TWO)) {
            saveSettings(SettingsKey.SEED_CALL_LEVEL_TWO_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_TWO.getKey() + ".");
        } else if (level.equals(Levels.THREE)) {
            saveSettings(SettingsKey.SEED_CALL_LEVEL_THREE_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_THREE.getKey() + ".");
        } else if (level.equals(Levels.FOUR)) {
            saveSettings(SettingsKey.SEED_CALL_LEVEL_FOUR_CONDITIONS, SettingsKey.SEED_CALL_LEVEL_FOUR.getKey() + ".");
        }
    }

    private void saveSettings(SettingsKey settingsKey, String keyPrefix) {
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
                Conditions remove = conditions.remove(0);
                replySuccessfully(event, remove);
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
            int id = idOption.getAsInt() - 1;
            if (id < 0 || id >= conditions.size()) {
                event.reply("Niepoprawne ID").setEphemeral(true).queue();
                return;
            }
            Conditions remove = conditions.remove(id);
            replySuccessfully(event, remove);
        }
    }

    private void replySuccessfully(@NotNull SlashCommandInteractionEvent event, @NotNull Conditions conditions) {
        event.reply("Usunięto warunek: Jeżeli " + conditions.getPlayersCount() +
                        " graczy przez " + conditions.getWithinMinutes() + " minut")
                .setEphemeral(true)
                .queue();
        saveSettings();
        log.info("Remove conditions {} for level: {}", conditions, level);
    }

    private void pullRoleId() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role");
        optional.ifPresent(s -> this.roleId = s);
    }

    private void pullMessages() {
        messages = messageService.findByLevel(level);
    }

    protected void pullMessagePerDay() {
        Optional<String> optional = settingsService.find(settingsKeyPerDay);
        if (optional.isEmpty()) {
            log.info("New settings property set {}={}", settingsKeyPerDay, 0);
            settingsService.save(settingsKeyPerDay, 0);
            return;
        }
        try {
            this.messagePerDay = Integer.parseInt(optional.get());
            if (messagePerDay < 0 || messagePerDay > MAX_PER_DAY) {
                throw new IllegalArgumentException("Message per day " + messagePerDay);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Settings property \"{}\" incorrect. Set default value={}", settingsKeyPerDay, 0);
            settingsService.save(settingsKeyPerDay, 0);
        }
    }

    private void pullMessagePerDayCount() {
        Optional<String> optional = settingsService.find(SettingsKey.SEED_CALL_LEVEL_ONE_COUNT);
        if (optional.isEmpty()) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_ONE_COUNT, 0);
            this.messagePerDayCount = 0;
            return;
        }
        if (!optional.get().chars().allMatch(Character::isDigit)) {
            settingsService.save(SettingsKey.SEED_CALL_LEVEL_ONE_COUNT, 0);
            this.messagePerDayCount = 0;
            return;
        }
        this.messagePerDayCount = Integer.parseInt(optional.get());
    }


    void setMaxAmount(@NotNull SlashCommandInteractionEvent event) {
        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 0 || count > MAX_PER_DAY) {
            event.reply("Niepoprawna wartość! Maksymalna - " + MAX_PER_DAY).setEphemeral(true).queue();
            log.error("Option incorrect - {}", count);
            return;
        }
        messagePerDay = count;
        settingsService.save(settingsKeyPerDay, count);
        event.reply("Ustawiono maksymalną ilość wiadomości - " + count).setEphemeral(true).queue();
        log.info("Set max amount {} for level: {}", count, level.getLevel());
    }

    public String getConditions() {
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

    public void sendMessage(String channelId) {
        if (messagePerDayCount >= messagePerDay) {
            log.info("Max message per day on this level");
            return;
        }
        Random random = new Random();
        int nextInt = random.nextInt(messages.size());
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild == null) {
            log.warn("Null guild");
            return;
        }
        TextChannel textChannel = guild.getTextChannelById(channelId);
        if (textChannel == null) {
            log.warn("Null channel");
            return;
        }
        StringBuilder builder = new StringBuilder();
        if (roleId != null) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                builder.append(role.getAsMention());
            }
        }
        addMessagePerDayCount();
        textChannel.sendMessage(builder.append(" ").append(messages.get(nextInt).getMessage())).queue();
        log.info("Sent seed call message");
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
                ", settingsKeyPerDay=" + settingsKeyPerDay +
                ", type=" + level +
                ", MAX_PER_DAY=" + MAX_PER_DAY +
                ", messagePerDayCount=" + messagePerDayCount +
                ", messagePerDay=" + messagePerDay +
                '}';
    }

    public void addMessage(SlashCommandInteractionEvent event) {
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
        messages.add(newMessage);
        event.reply("**Wiadomość dodana.**\n" + message).setEphemeral(true).queue();
        log.info("Message added");
        messageService.save(newMessage);
    }

    public void removeMessage(SlashCommandInteractionEvent event) {
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
        if (messages.size() == 0) {
            event.reply("Brak wiadomości na tym levelu").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(getMessagesAsDescription());
        if (roleId != null && DiscordBot.getJda().getRoleById(roleId) != null) {
            Role role = DiscordBot.getJda().getRoleById(roleId);
            if (role != null) {
                builder.addField("", role.getAsMention(), false);
            }
        }
        if (messages.size() > 10) {
            builder.setFooter("Strona 1");
            event.replyEmbeds(builder.build()).setComponents(ActionRow.of(getButtons())).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(builder.build()).setComponents().setEphemeral(true).queue();
    }

    @NotNull
    private Collection<? extends ItemComponent> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (messages.size() > 10) {
            buttons.add(Button.primary(ComponentId.SEED_CALL_BACK, "⮜"));
            buttons.add(Button.primary(ComponentId.SEED_CALL_NEXT, "⮞"));
        }
        return buttons;
    }

    @NotNull
    private String getMessagesAsDescription() {
        StringBuilder builder = new StringBuilder("## Wiadomości na levelu ").append(level.getLevel()).append("\n");
        for (int i = 0; i < messages.size() && i < 10; i++) {
            builder.append("ID: ").append(messages.get(i).getId()).append(" - ").append(messages.get(i).getMessage()).append("\n");
        }
        return builder.toString();
    }

    public int getMessageSize() {
        return messages.size();
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event) {
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
        builder.setDescription(getMessagesAsDescription(page));
        if (roleId != null && DiscordBot.getJda().getRoleById(roleId) != null) {
            Role role = DiscordBot.getJda().getRoleById(roleId);
            if (role != null) {
                builder.addField("", role.getAsMention(), false);
            }
        }
        builder.setFooter("Strona " + page);
        event.getMessage().editMessageEmbeds(builder.build()).queue();
    }

    @NotNull
    private String getMessagesAsDescription(int page) {
        StringBuilder builder = new StringBuilder("## Wiadomości na levelu ").append(level.getLevel()).append("\n");
        for (int i = (page - 1) * 10; i < messages.size() && i < (page - 1) * 10 + 10; i++) {
            builder.append("ID: ").append(messages.get(i).getId()).append(" - ").append(messages.get(i).getMessage()).append("\n");
        }
        return builder.toString();
    }

    public void setRole(String roleId) {
        this.roleId = roleId;
        settingsService.save(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role", roleId);
    }

    public void deleteRole() {
        this.roleId = null;
        settingsService.deleteByKey(SettingsKey.SEED_CALL_LEVEL.getKey() + "." + level.getLevel() + ".role");
    }
}
