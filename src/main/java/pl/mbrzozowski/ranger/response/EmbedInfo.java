package pl.mbrzozowski.ranger.response;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.configuration.content.Content;
import pl.mbrzozowski.ranger.configuration.content.ContentService;
import pl.mbrzozowski.ranger.configuration.content.Field;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventChanges;
import pl.mbrzozowski.ranger.exceptions.ContentNotFoundException;
import pl.mbrzozowski.ranger.guild.ComponentId;
import pl.mbrzozowski.ranger.guild.RangersGuild;
import pl.mbrzozowski.ranger.helpers.StringProvider;
import pl.mbrzozowski.ranger.helpers.Users;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EmbedInfo extends EmbedCreator {


    public static void recruiter(@NotNull MessageReceivedEvent event) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setDescription("# Podanie\n" +
                "Chcemy nasze wieloletnie doświadczenie przekazać kolejnym Rangersom. Nasza gra opiera się na wzajemnej komunikacji i skoordynowanym działaniu. " +
                "Jako grupa, pielęgnujemy dobrą atmosferę i przyjazne, dojrzałe relacje między członkami naszego klanu, a także polską społecznością.");
        builder.addField("Złóż podanie do klanu klikając przycisk PONIŻEJ", "", false);
        builder.addField("Wymagamy", """
                - znajomość zasad rozgrywki w Squad
                - gra zespołowa (używamy TeamSpeak 3)
                - kultura osobista
                - duża ilość wolnego czasu
                - brak VAC bana w ciągu 2 ostatnich lat
                - minimum 300h w grze
                - znajomość regulaminu LTW
                - chęć integracji z członkami klanu""", false);
        event.getChannel().sendMessageEmbeds(builder.build())
                .setComponents(ActionRow.of(Button.success(ComponentId.NEW_RECRUIT, "Podanie")))
                .queue();
    }

    /**
     * Sends information about closed channel
     *
     * @param signature user who closing channel
     * @param channel   channel which is closing
     */
    public static void closeServerServiceChannel(String signature, @NotNull MessageChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
        builder.setTitle("Kanał zamknięty");
        builder.setDescription("Kanał zamknięty przez " + signature + ".");
        channel.sendMessageEmbeds(builder.build())
                .setComponents(ActionRow.of(getButtons(signature)))
                .queue();
    }

    @NotNull
    private static List<Button> getButtons(@NotNull String signature) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.danger(ComponentId.REMOVE_SERVER_SERVICE_CHANNEL, "Usuń kanał"));
        if (signature.equalsIgnoreCase("Ranger - brak aktywności")) {
            buttons.add(Button.success(ComponentId.SERVER_SERVICE_OPEN_NO_CLOSE, "Otwórz i nie zamykaj automatycznie"));
        }
        return buttons;
    }

    public static void confirmCloseChannel(@NotNull ButtonInteractionEvent event) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.QUESTION);
        builder.setTitle("Do you want close the ticket?");
        event.reply("")
                .setEmbeds(builder.build())
                .setActionRow(Button.success(ComponentId.CLOSE_YES, "Yes"),
                        Button.danger(ComponentId.CLOSE_NO, "No"))
                .queue();
    }

    public static void confirmRemoveChannel(@NotNull TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.QUESTION);
        builder.setTitle("Potwierdź czy chcesz usunąć kanał?");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.success(ComponentId.REMOVE_YES, "Tak"),
                        Button.danger(ComponentId.REMOVE_NO, "Nie"))
                .queue();
    }

    /**
     * Wyświetla informację że kanał został usunięty i że za chwilę zniknie.
     *
     * @param event Button interaction
     */
    public static void removedChannel(@NotNull ButtonInteractionEvent event) {
        event.reply("Kanał wkrótce zostanie usunięty.").setEphemeral(true).queue();
    }

    public static void endNegative(String drillId, String recruitId, @NotNull TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle(EmbedSettings.RESULT + "NEGATYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(drillId));
        channel.sendMessage("<@" + recruitId + ">").setEmbeds(builder.build()).queue();

        JDA jda = DiscordBot.getJda();
        jda.openPrivateChannelById(recruitId).queue(privateChannel -> {
            builder.setDescription("Rekrutacja do klanu Rangers Polska zostaje zakończona z wynikiem NEGATYWNYM!");
            privateChannel.sendMessageEmbeds(builder.build()).queue((s) -> log.info("Private message send"),
                    new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
                            (ex) -> log.info("Cannot send messages to this user in private channel")));
        });
    }

    public static void endPositive(String drillId, String recruitId, @NotNull TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
        builder.setTitle(EmbedSettings.RESULT + "POZYTYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(drillId));
        channel.sendMessage("Gratulacje <@" + recruitId + ">").setEmbeds(builder.build()).queue();
    }

    /**
     * Jeżeli użytkownik nie jest botem usuwa wiadomość i wysyła informację że kanał służy do logowania i tam nie piszemy.
     *
     * @param event Wydarzenie napisania wiadomości na kanale tekstowym.
     */
    public static void noWriteOnLoggerChannel(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getMessage().delete().submit();
            User userById = DiscordBot.getJda().getUserById(event.getAuthor().getId());
            if (userById != null) {
                userById.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
                    builder.setTitle("Zachowajmy porządek!");
                    builder.setDescription("Panie administratorze! Zachowajmy czystość na kanale do loggowania. Proszę nie wtrącać się w moje wypociny.");
                    builder.setFooter(getFooter());
                    privateChannel.sendMessageEmbeds(builder.build()).queue();
                });
            }
        }
    }

    /**
     * Wysyła informację że pomyślnie wyłączono przypomnienia dla eventów.
     *
     * @param userID ID użytkownika
     */
    public static void reminderOff(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
                builder.setTitle("Przypomnienia wyłączone.");
                builder.setDescription("Aby włączyć ponownie przypomnienia użyj komendy **!reminder On**");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
    }

    /**
     * Wysyła informację że pomyślnie włączono przypomnienia dla eventów.
     *
     * @param userID ID użytkownika
     */
    public static void reminderOn(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
                builder.setTitle("Przypomnienia włączone.");
                builder.setFooter("Więcej informacji i ustawień powiadomień pod komendą !help Reminder");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
    }

    /**
     * Wysyła do użytkownika o ID userID wiadomość o zmianach w evencie.
     *
     * @param userID     ID użytkownika
     * @param whatChange jaka zmiana zostala wykonana
     * @param dateTime   data i czas
     */
    public static void sendInfoChanges(String userID, Event event, @NotNull EventChanges whatChange, String dateTime) {
        String description = "";
        if (whatChange.equals(EventChanges.CHANGES)) {
            description = "Zmieniona data lub czas wydarzenia na które się zapisałeś.";
        } else if (whatChange.equals(EventChanges.REMOVE)) {
            description = "Wydarzenie zostaje odwołane.";
        }
        String link = "[" + event.getName() + "](" + RangersGuild.getLinkToMessage(event.getChannelId(), event.getMsgId()) + ")";
        User userById = DiscordBot.getJda().getUserById(userID);
        String finalDescription = description + " Sprawdź szczegóły!";
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
                builder.setThumbnail(EmbedSettings.THUMBNAIL);
                builder.setTitle("**UWAGA:** Zmiany w wydarzeniu.");
                builder.setDescription(finalDescription);
                builder.addField("Szczegóły eventu", link + "\n:date: " + dateTime, false);
                privateChannel.sendMessageEmbeds(builder.build()).queue();
                log.info("USER: {} -  wysłałem powiadomienie", userID);
            });
        }
    }

    public static void seedersRoleJoining(@NotNull TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
        builder.setTitle("SQUAD SERVER SEEDER");
        builder.addField("", "Jeśli chcesz pomóc nam w rozkręcaniu naszego serwera. Możesz przypisać sobię rolę klikając w poniższy przycisk by otrzymywać ping.", false);
        builder.addField("", "If you would like to help us seed our server you can add role below to receive a ping.", false);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.SEED_ROLE, "Add/Remove Seed Role ").withEmoji(Emoji.fromUnicode("\uD83C\uDF31"))).queue();
    }

    public static void serverService(@NotNull MessageReceivedEvent event, @NotNull ContentService contentService) {
        Content content;
        String key = "serverSupport";
        try {
            content = contentService.getContent(key);
        } catch (ContentNotFoundException e) {
            event.getAuthor()
                    .openPrivateChannel()
                    .queue(privateChannel
                            -> privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                            "\tERROR - Nie można odnaleźć wartości dla klucza: \"" + key + "\" w pliku \"content.json\"```").queue());
            throw new RuntimeException("Error retrieving content for key: " + key, e);
        } catch (IOException e) {
            event.getAuthor()
                    .openPrivateChannel()
                    .queue(privateChannel ->
                            privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                                    "\tERROR - Nie można odczytać pliku \"content.json\"").queue());
            throw new RuntimeException("Can not read a file \"content.json\"", e);
        }
        try {
            EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
            builder.setTitle(content.getTitle());
            builder.setDescription(content.getDescription());
            for (Field field : content.getFields()) {
                builder.addField(field.getName(), field.getValue(), field.isInline());
            }
            event.getChannel().sendMessage(content.getMessage()).setEmbeds(builder.build()).setActionRow(
                    Button.primary(ComponentId.SERVER_SERVICE_REPORT, content.getButtons().get(0).getLabel()).withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_RED)),
                    Button.primary(ComponentId.SERVER_SERVICE_UNBAN, content.getButtons().get(1).getLabel()).withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_BLUE)),
                    Button.primary(ComponentId.SERVER_SERVICE_CONTACT, content.getButtons().get(2).getLabel()).withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_GREEN))).queue();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            event.getAuthor()
                    .openPrivateChannel()
                    .queue(privateChannel -> privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                            "\tERROR - Błąd tworzenia embed. Więcej szczegółów w \"rangerbot.log\"```").queue());
            throw new RuntimeException("Can not create embed", e);
        }
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param userID  pinguje usera z tym ID
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedReport(String userID, @NotNull TextChannel channel, @NotNull ContentService contentService) {
        Content content;
        String key = "embedReport";
        content = getContent(contentService, key);
        embedBuilder(userID, channel, contentService, content, key, Color.RED, ThumbnailType.WARNING);
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param userID  pinguje usera z tym ID
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedUnban(String userID, @NotNull TextChannel channel, @NotNull ContentService contentService) {
        Content content;
        String key = "embedUnban";
        content = getContent(contentService, key);
        embedBuilder(userID, channel, contentService, content, key, Color.BLUE, ThumbnailType.DEFAULT);
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param userID  pinguje usera z tym ID
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedContact(String userID, @NotNull TextChannel channel, @NotNull ContentService contentService) {
        Content content;
        String key = "embedContact";
        content = getContent(contentService, key);
        embedBuilder(userID, channel, contentService, content, key, Color.GREEN, ThumbnailType.WARNING);
    }

    private static void embedBuilder(String userID,
                                     @NotNull TextChannel channel,
                                     @NotNull ContentService contentService,
                                     @NotNull Content content,
                                     String key,
                                     Color color,
                                     ThumbnailType thumbnailType) {
        try {
            EmbedBuilder builder = getEmbedBuilder(color, thumbnailType);
            builder.setTitle(content.getTitle());
            builder.setDescription(content.getDescription());
            for (Field field : content.getFields()) {
                builder.addField(field.getName(), field.getValue(), field.isInline());
            }
            channel.sendMessage(contentService.textFormat(content.getMessage(), userID))
                    .setEmbeds(builder.build())
                    .setActionRow(Button.primary(ComponentId.CLOSE, content.getButtons().get(0).getLabel()).withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                    .queue(message -> message.pin().queue());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Users.getDevUser()
                    .openPrivateChannel()
                    .queue(privateChannel -> privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                            "\tERROR - Błąd tworzenia embed(" + key + "\"). Więcej szczegółów w \"rangerbot.log\"```").queue());
            throw new RuntimeException("Can not create embed", e);
        }
    }

    private static Content getContent(@NotNull ContentService contentService, String key) {
        Content content;
        try {
            content = contentService.getContent(key);
        } catch (ContentNotFoundException e) {
            Users.getDevUser()
                    .openPrivateChannel()
                    .queue(privateChannel
                            -> privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                            "\tERROR - Nie można odnaleźć wartości dla klucza: \"" + key + "\" w pliku \"content.json\"```").queue());
            throw new RuntimeException("Error retrieving content for key: " + key, e);
        } catch (IOException e) {
            Users.getDevUser()
                    .openPrivateChannel()
                    .queue(privateChannel ->
                            privateChannel.sendMessage("```" + StringProvider.getDateAndTime() +
                                    "\tERROR - Nie można odczytać pliku \"content.json\"").queue());
            throw new RuntimeException("Can not read a file \"content.json\"", e);
        }
        return content;
    }

    public static void recruitAnonymousComplaintsFormOpening(@NotNull TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_CONFIRM);
        builder.setTitle("Anonimowe zgłoszenia");
        builder.addField("Zgłoś anonimowo sytuację używając przycisku poniżej", "", false);
        textChannel
                .sendMessageEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.OPEN_FORM_ANONYMOUS_COMPLAINTS, "Formularz"))
                .queue();
    }

    public static void recruitAccepted(String userName, @NotNull TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_BLUE);
        builder.setTitle("Rozpoczęto rekrutację");
        builder.setDescription("Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem wszystkich członków klanu.");
        builder.setFooter("Podpis: " + userName);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void recruitNotAccepted(String userName, @NotNull TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Podanie odrzucone");
        builder.setFooter("Podpis: " + userName);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void warningMaxRecruits() {
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.DRILL_INSTRUCTOR_HQ);
        if (textChannel != null) {
            textChannel.sendMessage("**Brak wolnych miejsc. Rekrutacja zamknięta.**\nOsiągnięto maksymalną ilość kanałów w kategorii.").queue();
        }
    }

    public static void warningFewSlots() {
        TextChannel textChannel = RangersGuild.getTextChannel(RangersGuild.ChannelsId.DRILL_INSTRUCTOR_HQ);
        if (textChannel != null) {
            textChannel.sendMessage("**Pozostały 2 lub mniej miejsc dla rekrutów.**").queue();
        }
    }
}
