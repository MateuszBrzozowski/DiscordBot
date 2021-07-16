package bot.event;


import embed.*;
import event.Event;
import event.EventsGenerator;
import event.EventsGeneratorModel;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import model.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private List<MemberMy> clanMember = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        boolean radKlan = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(), RoleID.RADA_KLANU);
        boolean clanMember = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(), RoleID.CLAN_MEMBER_ID);
        Event matches = RangerBot.getMatches();
        Recruits recruits = RangerBot.getRecruits();

        if (clanMember) {
            if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
                event.getMessage().delete().submit();
                if (radKlan) new Recruiter(event);
            } else if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.RANGER_BOT_LOGGER)) {
                new EmbedNoWriteOnLoggerChannel(event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATIVE)) {
                event.getMessage().delete().submit();
                if (radKlan) new EmbedNegative(event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POSITIVE)) {
                event.getMessage().delete().submit();
                if (radKlan) new EmbedPositive(event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.GENERATOR)) {
                event.getMessage().delete().submit();
                EventsGeneratorModel eventsGeneratorModel = RangerBot.getEventsGeneratorModel();
                String authorID = event.getAuthor().getId();
                int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
                if (indexOfGenerator == -1) {
                    EventsGenerator eventsGenerator = new EventsGenerator(event);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                } else {
                    new EmbedYouHaveActiveEventGenerator(event);
                    eventsGeneratorModel.cancelEventGenerator(event);
                    new EmbedICreateNewGenerator(event);
                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
                    EventsGenerator eventsGenerator = new EventsGenerator(event);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                }

            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.GENERATOR_HERE)) {
                event.getMessage().delete().submit();
                EventsGeneratorModel eventsGeneratorModel = RangerBot.getEventsGeneratorModel();
                String authorID = event.getAuthor().getId();
                int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
                if (indexOfGenerator == -1) {
                    EventsGenerator eventsGenerator = new EventsGenerator(event);
                    eventsGenerator.setHere(true);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                } else {
                    new EmbedYouHaveActiveEventGenerator(event);
                    eventsGeneratorModel.cancelEventGenerator(event);
                    new EmbedICreateNewGenerator(event);
                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
                    EventsGenerator eventsGenerator = new EventsGenerator(event);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                }

            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
                event.getMessage().delete().submit();
                String userID = event.getMessage().getAuthor().getId();
                matches.createNewChannel(event, userID);
            } else if (message.length > 1 && message.length < 100 && message[0].equalsIgnoreCase(Commands.NAME)) {
                if (matches.checkChannelIsInEventCategory(event)) {
                    String name = getNameFromUser(message);
                    event.getMessage().delete().submit();
                    event.getChannel().getManager().setName(name).queue();
                }
            } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
                event.getMessage().delete().submit();
                matches.createNewEventFrom3Data(message, event);
            } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
                event.getMessage().delete().submit();
                matches.createNewEventFrom4Data(message, event);
            } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
                event.getMessage().delete().submit();
                matches.createNewEventFrom3DataHere(message, event);
            } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
                event.getMessage().delete().submit();
                matches.createNewEventFrom4DataHere(message, event);
            } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
                event.getMessage().delete().submit();
                matches.createNewEventFromSpecificData(message, event);
            } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
                event.getMessage().delete().submit();
                matches.createNewEventFromSpecificData(message, event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.CLOSE)) {
                event.getMessage().delete().submit();
                if (radKlan) recruits.closeChannel(event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REOPEN)) {
                event.getMessage().delete().submit();
                if (radKlan) recruits.reOpenChannel(event);
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.HELPS)) {
                event.getMessage().delete().submit();
                new EmbedHelp(event.getMessage().getAuthor().getId());
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
                event.getMessage().delete().submit();
                if (radKlan) {
                    logger.info("Usuwanie kanału.");
                    String channelID = event.getChannel().getId();
                    if (recruits.isRecruitChannel(channelID)) {
                        recruits.deleteChannel(event);
                    } else if (matches.isActiveMatchChannelID(channelID) >= 0) {
                        matches.deleteChannel(event);
                    }
                }
            }
        }

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.DICE)) {
            event.getMessage().delete().submit();
            new EmbedDice(event);
        } else if (message.length > 1 && message[0].equalsIgnoreCase(Commands.DICE)) {
            event.getMessage().delete().submit();
            DiceGame diceGame = new DiceGame(message, event);
            DiceGames diceGames = RangerBot.getDiceGames();
            diceGames.addGame(diceGame);
        } else {
            //wpisane cokolwiek przez kogokowliek
            //sprawdzic czy jest aktywna gra na tym kanale
            //jak tak to ją dokonczyć z informacją
            DiceGames diceGames = RangerBot.getDiceGames();
            if (!event.getAuthor().isBot()) {
                if (diceGames.isActiveGameOnChannelID(event.getChannel().getId())) {
                    event.getMessage().delete().submit();
                    diceGames.play(event);
                }
            }
        }
    }

    private String getNameFromUser(String[] message) {
        String result = "";
        for (int i = 1; i < message.length; i++) {
            result += message[i] + " ";
        }
        return result;
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!isClanMember(event)) return;

        String[] message = event.getMessage().getContentRaw().split(" ");
        Event matches = RangerBot.getMatches();

        EventsGeneratorModel eventsGeneratorModel = RangerBot.getEventsGeneratorModel();
        int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(event.getAuthor().getId());

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(Commands.GENERATOR)) {
            String authorID = event.getAuthor().getId();
            if (eventsGeneratorModel.userHaveActiveGenerator(authorID) == -1) {
                EventsGenerator eventsGenerator = new EventsGenerator(event);
                eventsGeneratorModel.addEventsGenerator(eventsGenerator);
            } else {
                new EmbedYouHaveActiveEventGenerator(event);
                eventsGeneratorModel.cancelEventGenerator(event);
                new EmbedICreateNewGenerator(event);
                eventsGeneratorModel.removeGenerator(indexOfGenerator);
                EventsGenerator eventsGenerator = new EventsGenerator(event);
                eventsGeneratorModel.addEventsGenerator(eventsGenerator);
            }
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.HELPS)) {
            new EmbedHelp(event.getAuthor().getId());
        } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom3Data(message, event);
        } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom4Data(message, event);
        } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            event.getMessage().delete().submit();
            matches.createNewEventFromSpecificData(message, event);
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
            String userID = event.getMessage().getAuthor().getId();
            matches.createNewChannel(event, userID);
        } else if (indexOfGenerator >= 0) {
            if (event.getMessage().getContentDisplay().equalsIgnoreCase("!cancel")) {
                eventsGeneratorModel.cancelEventGenerator(event);
                eventsGeneratorModel.removeGenerator(indexOfGenerator);
            } else eventsGeneratorModel.saveAnswerAndNextStage(event, indexOfGenerator);
        } else {
            sendMessage(event);
        }
    }

    private boolean isClanMember(PrivateMessageReceivedEvent event) {
        List<Guild> guilds = event.getJDA().getGuilds();
        for (int i = 0; i < guilds.size(); i++) {
            if (guilds.get(i).getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
                List<Member> members = guilds.get(i).getMembers();
                for (int j = 0; j < members.size(); j++) {
                    if (members.get(j).getId().equalsIgnoreCase(event.getAuthor().getId())) {
                        List<Role> roles = members.get(j).getRoles();
                        for (int k = 0; k < roles.size(); k++) {
                            if (roles.get(k).getId().equalsIgnoreCase(RoleID.CLAN_MEMBER_ID)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    private void sendMessage(@NotNull PrivateMessageReceivedEvent event) {
        String[] msg = {"Nie rozumiem", "O co Ci chodzi?", "Niestety, nie potrafię Cię zrozumieć!", "Słucham?", "Ale że jak?", "Co?",
                "Ja dopiero rosnę na sile. Na chwilę obecną nie rozumiem Ciebie. Przepraszam", "Chcesz bana?", "Proszę mnie nie drażnić!",
                "Proszę mnie nie denerwować! Ja jestem malutki. Nie rozumiem Ciebie!", "Dlaczego mi to reboisz? Zostaw mnie w spokoju",
                "Idź i nie pisz do mnie. BRZOZAAAA!!!! ratuj", "Bóg jest odpowiedzią na wszystkie pytania", "Stanę przed wejście do burdelu, żeby złapać w nią twoją mamuśkę",
                "Gdybyś był moim mężem, wsypałabym ci truciznę do herbaty.", "Jak nazywa się człowiek nóż? Janusz!", "Po co dresiiarz idzie do lasu? Poziomki."};
        Random random = new Random();
        event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. Jeżeli potrzebujesz pomocy. Wpisz !help").queue();
            });
        });
    }
}



