package pl.mbrzozowski.ranger.embed;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.event.Event;
import pl.mbrzozowski.ranger.event.EventChanges;
import pl.mbrzozowski.ranger.helpers.*;

import java.awt.*;

@Slf4j
public class EmbedInfo extends EmbedCreator {


    public static void recruiter(MessageReceivedEvent event) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("PODANIE");
        builder.addField("", "Chcemy nasze wieloletnie doświadczenie przekazać kolejnym Rangersom. Nasza gra opiera się na wzajemnej komunikacji i skoordynowanym działaniu. " +
                "Jako grupa, pielęgnujemy dobrą atmosferę i przyjazne, dojrzałe relacje między członkami naszego klanu, a także polską społecznością. \n", false);
        builder.addField("Złóż podanie do klanu klikając przycisk PONIŻEJ", "", false);
        event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.NEW_RECRUT, "Podanie")).queue();
    }

    /**
     * Wyświetla informację, że kanał podany w parametrze został zamknięty.
     *
     * @param userID  ID użytkownika który zamyka kanał
     * @param channel Kanał który został zamknięty.
     */
    public static void closeChannel(String userID, MessageChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Kanał zamknięty");
        builder.setDescription("Kanał zamknięty przez " + Users.getUserNicknameFromID(userID) + ".");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.danger(ComponentId.REMOVE, "Usuń kanał"))
                .queue();
    }

    public static void confirmCloseChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.QUESTION);
        builder.setTitle("Do you want close the ticket?");
        channel.sendMessageEmbeds(builder.build())
                .setActionRow(Button.success("closeYes", "Yes"),
                        Button.danger("closeNo", "No"))
                .queue();
    }

    public static void confirmRemoveChannel(TextChannel channel) {
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
     * @param channel Kanał który został usunięty.b
     */
    public static void removedChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("Kanał usunięty.");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void endNegative(String drillId, String recruitId, TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle(EmbedSettings.RESULT + "NEGATYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem NEGATYWNYM!");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(drillId));
        channel.sendMessage("<@" + recruitId + ">").setEmbeds(builder.build()).queue();
        String oldName = channel.getName();
        channel.getManager().setName(EmbedSettings.RED_CIRCLE + oldName).queue();

        JDA jda = DiscordBot.getJda();
        jda.openPrivateChannelById(recruitId).queue(privateChannel -> {
            builder.setDescription("Rekrutacja do klanu Rangers Polska zostaje zakończona z wynikiem NEGATYWNYM!");
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    public static void endPositive(String drillId, String recruitId, TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle(EmbedSettings.RESULT + "POZYTYWNY");
        builder.setDescription("Rekrutacja zostaje zakończona z wynikiem POZYTYWNYM!");
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        builder.setFooter("Podpis: " + Users.getUserNicknameFromID(drillId));
        channel.sendMessage("Gratulacje <@" + recruitId + ">").setEmbeds(builder.build()).queue();
        String oldName = channel.getName();
        channel.getManager().setName(EmbedSettings.GREEN_CIRCLE + oldName).queue();
    }

    /**
     * Wysyła do użytkownika o ID userID informację że jest już w klanie nie może złożyć podania na rekrutację.
     *
     * @param event ButtonInteractionEvent
     */
    public static void userIsInClanMember(ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**\n" +
                        "Jesteś już w naszym klanie dzbanie!")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkonik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Jest już w naszym klanie.");
    }

    /**
     * Wysyła do użytkownika wiadomość że rekrutacja została tymczasowo zamknięta
     *
     * @param event ButtonInteractionEvent
     */
    public static void maxRecrutis(ButtonInteractionEvent event) {
        event.reply("**REKRTUACJA DO KLANU RANGERS POLSKA TYMCZASOWO ZAMKNIĘTA!**")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkonik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Maksymalna liczba kanałów w kategorii StrefaRekruta.");
    }

    /**
     * Wysyła do użytkownika o ID userID informację że jest już w innym klanie nie może złożyć podania na rekrutację.
     *
     * @param event ButtonInteractionEvent
     */
    public static void userIsInClan(ButtonInteractionEvent event) {
        event.reply("**NIE MOŻESZ ZŁOŻYĆ PODANIA DO NASZEGO KLANU!**\n" +
                        "Masz przypisaną rolę innego klanu na naszym discordzie.")
                .setEphemeral(true)
                .queue();
        RangerLogger.info("Użytkonik [" + Users.getUserNicknameFromID(event.getUser().getId()) + "] chciał złożyć podanie. Ma przypisaną rolę innego klanu.");
    }

    /**
     * Wysyła informację, że użytkownik o ID miał już otwarty generator.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void userHaveActiveEventGenerator(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
                builder.setTitle("MIAŁEŚ AKTYWNY GENERATOR");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
    }

    /**
     * Wysyła informację do użytkownika o ID że otwierany jest dla niego nowy kanał.
     *
     * @param userID ID użytkownika do którego jest wysyłana informacja.
     */
    public static void createNewGenerator(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
                builder.setTitle("OTWIERAM NOWY GENERATOR");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
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
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
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
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
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
    public static void sendInfoChanges(String userID, Event event, EventChanges whatChange, String dateTime) {
        String description = "";
        if (whatChange.equals(EventChanges.CHANGES)) {
            description = "Zmieniona data lub czas wydarzenia na które się zapisałeś.";
        } else if (whatChange.equals(EventChanges.REMOVE)) {
            description = "Wydarzenie zostaje odwołane.";
        }
        String link = "[" + event.getName() + "](https://discord.com/channels/" + CategoryAndChannelID.RANGERSPL_GUILD_ID + "/" + event.getChannelId() + "/" + event.getMsgId() + ")";
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

    /**
     * Wysyła informację że generowanie listy zostało przerwane.
     *
     * @param userID ID użytkownika do którego wysyłana jest informacja
     */
    public static void cancelEventGenerator(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
                builder.setTitle("GENEROWANIE LISTY ZOSTAŁO PRZERWANE");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
    }

    public static void cancelEventEditing(String userID) {
        User userById = DiscordBot.getJda().getUserById(userID);
        if (userById != null) {
            userById.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("Edytor zamknięty");
                builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                privateChannel.sendMessageEmbeds(builder.build()).queue();
            });
        }
    }

    public static void seedersRoleJoining(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("SQUAD SERVER SEEDER");
        builder.addField("", "Jeśli chcesz pomóc nam w rozkręcaniu naszego serwera. Możesz przypisać sobię rolę klikając w poniższy przycisk by otrzymywać ping.", false);
        builder.addField("", "If you would like to help us seed our server you can add role below to receive a ping.", false);
        builder.setThumbnail(EmbedSettings.THUMBNAIL);
        channel.sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.SEED_ROLE, "Add/Remove Seed Role ").withEmoji(Emoji.fromUnicode("\uD83C\uDF31"))).queue();
    }

    /**
     * Formatka z opisem jak stworzyć ticket.
     *
     * @param channel Kanał na którym wstawiana jest formatka.
     */
    public static void serverService(MessageChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Rangers Polska Servers - Create Ticket :ticket:");
        builder.addField("", "Jeśli potrzebujesz pomocy admina naszych serwerów, " +
                "kliknij w odpowiedni przycisk poniżej.", false);
        builder.addField("--------------------", "If you need help of Rangers Polska Servers Admins, " +
                "please react with the correct button below.", false);
        channel.sendMessageEmbeds(builder.build()).setActionRow(
                Button.primary(ComponentId.SERVER_SERVICE_REPORT, "Report Player").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_RED)),
                Button.primary(ComponentId.SERVER_SERVICE_UNBAN, "Unban appeal").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_BLUE)),
                Button.primary(ComponentId.SERVER_SERVICE_CONTACT, "Contact With Admin").withEmoji(Emoji.fromUnicode(EmbedSettings.BOOK_GREEN))).queue();
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedReport(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.WARNING);
        builder.setTitle("Report player");
        builder.addField("", """
                Zgłoś gracza według poniższego formularza.

                1. Podaj nick.
                2. Opisz sytuację i podaj powód dlaczego zgłaszasz gracza.
                3. Podaj nazwę serwera.
                4. Dodaj dowody. Screenshot lub podaj link do wideo (np. Youtube).""", false);
        builder.addField("--------------------", """
                Report player according to the form below.

                1. Player nick.
                2. Describe of bad behaviour.
                3. Server name.
                4. Add evidence. Screenshot or video link (e.g. Youtube).""", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.CLOSE, "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedUnban(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLUE, ThumbnailType.DEFAULT);
        builder.setTitle("Unban player");
        builder.addField("", """
                Napisz tutaj jeżeli chcesz odowłać swój ban.
                1. Podaj swój nick i/lub steamid.
                2. Podaj nazwę serwera.""", false);
        builder.addField("--------------------", """
                Write here if you want to revoke your ban.
                1. Provide your ingame nick and/or steamid.
                2. Server name.""", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.CLOSE, "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    /**
     * Wysyła powitalną formatkę z opisem do czego dany kanał służy i co należy zrobić.
     *
     * @param channel Kanał na którym wysyłana jest wiadomość
     */
    public static void sendEmbedContact(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.GREEN, ThumbnailType.DEFAULT);
        builder.setTitle("Contact with Admin");
        builder.addField("", "Napisz tutaj jeżeli masz jakiś problem z którymś z naszych serwerów, dodaj screenshoty, nazwę serwera. " +
                "Twój nick w grze lub/i steamId64.", false);
        builder.addField("--------------------", "Please describe your problem with more details, " +
                "screenshots, servername the issue occured on and related steamId64", false);
        channel.sendMessage("<@&" + RoleID.SERVER_ADMIN + ">")
                .setEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.CLOSE, "Close ticket").withEmoji(Emoji.fromUnicode(EmbedSettings.LOCK)))
                .queue(message -> message.pin().queue());
    }

    public static void notConnectedAccount(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK, ThumbnailType.DEFAULT);
        builder.setTitle("Your discord account isn't linked to your Steam profile.");
        builder.setDescription("""
                If you want to link your discord account to your steam account use the command **!profile <steam64ID>**

                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.\s
                *!profile 76561197990543288*""");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void connectSuccessfully(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_GREEN);
        builder.setTitle("Successfully");
        builder.setDescription("Your discord account is linked to your Steam profile.\n" +
                "You can use command **!stats** to view your statistic from our server.");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void connectUnSuccessfully(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Steam64ID is not valid");
        builder.setDescription("""
                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.\s
                *!profile 76561197990543288*""");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void youCanCheckStatsOnChannel(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setDescription("You can check your stats on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void noDataToShow(TextChannel channel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setTitle("No data to show.");
        builder.setDescription("""
                If you played on our server and there is no data, please check your Steam64ID and update it by command !profile.

                Your Steam64ID - you can find it by pasting your link to steam profile here https://steamid.io/

                e.g.\s
                *!profile 76561197990543288*""");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void youCanLinkedYourProfileOnChannel(TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(Color.BLACK);
        builder.setDescription("Use command !profile on channel <#" + CategoryAndChannelID.CHANNEL_STATS + ">");
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void noActiveEvents(TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.INF_RED);
        builder.setTitle("Brak aktywnych eventów");
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void recrutOpinionsFormOpening(MessageReceivedEvent messageReceived) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Rekrut opinie");
        builder.addField("Otwórz formularz by wystawić opinię na temat rekruta.", "", false);
        messageReceived.getTextChannel().sendMessageEmbeds(builder.build())
                .setActionRow(Button.primary(ComponentId.OPEN_FORM, "Otwórz formularz"))
                .queue();
    }

    public static void recruitAccepted(String userName, TextChannel textChannel) {
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setDescription("Rozpoczęto rekrutację");
        builder.setFooter("Podpis: " + userName);
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public static void sendRoles(MessageReceivedEvent messageReceived) {
        SelectMenu roles = RoleID.getRoleToSelectMenu();
        EmbedBuilder builder = getEmbedBuilder(EmbedStyle.DEFAULT);
        builder.setTitle("Discord role");
        builder.setDescription("Add/Remove a role by selecting it from the list below.");
        messageReceived.getTextChannel()
                .sendMessageEmbeds(builder.build())
                .setActionRow(roles)
                .queue();
    }
}
