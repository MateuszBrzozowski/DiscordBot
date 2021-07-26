package bot.event;


import embed.*;
import event.Event;
import event.EventsGenerator;
import event.EventsGeneratorModel;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import model.*;
import net.dv8tion.jda.api.JDA;
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
import ranger.Repository;
import recrut.Recruits;

import java.util.ArrayList;
import java.util.List;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());
    private List<MemberMy> clanMember = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        boolean radKlan = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(), RoleID.RADA_KLANU);
        boolean clanMember = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(), RoleID.CLAN_MEMBER_ID);
        Event matches = Repository.getEvent();
        Recruits recruits = Repository.getRecruits();

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
            } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ACCEPT_RECRUT)) {
                event.getMessage().delete().submit();
                if (radKlan) recruits.acceptRecrut(message[1], event.getChannel(), event.getAuthor());
            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.GENERATOR)) {
                event.getMessage().delete().submit();
                EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
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
                EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
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
                    String name = getNewChannelNameFromMsg(message);
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
            DiceGames diceGames = Repository.getDiceGames();
            diceGames.addGame(diceGame);
        } else {
            DiceGames diceGames = Repository.getDiceGames();
            if (!event.getAuthor().isBot()) {
                if (diceGames.isActiveGameOnChannelID(event.getChannel().getId())) {
                    event.getMessage().delete().submit();
                    diceGames.play(event);
                }
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!isClanMember(event)) return;

        String[] message = event.getMessage().getContentRaw().split(" ");
        Event matches = Repository.getEvent();

        EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(event.getAuthor().getId());

//        if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ACCEPT_RECRUT)) {
//            Recruits recruits = RangerBot.getRecruits();
//            recruits.acceptRecrut(message[1], event.getChannel(), event.getAuthor());
//        }

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
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.HELP_REMINDER)) {
            new EmbedHelpReminder(event.getAuthor().getId());
        } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom3Data(message, event);
        } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFrom4Data(message, event);
        } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            matches.createNewEventFromSpecificData(message, event);
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
            String userID = event.getMessage().getAuthor().getId();
            matches.createNewChannel(event, userID);
        } else if (indexOfGenerator >= 0) {
            if (event.getMessage().getContentDisplay().equalsIgnoreCase("!cancel")) {
                eventsGeneratorModel.cancelEventGenerator(event);
                eventsGeneratorModel.removeGenerator(indexOfGenerator);
            } else eventsGeneratorModel.saveAnswerAndNextStage(event, indexOfGenerator);
        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.DELETE_EVENT)) {
            matches.removeEvent(message[1]);
        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
            matches.disableButtons(message[1]);
        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
            matches.enableButtons(message[1]);
        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.TIME)) {
            matches.changeTime(message[1], message[2]);
        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.DATE)) {
            matches.changeDate(message[1], message[2]);
        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.STATUS)) {
            sendStatus(event.getAuthor().getId());
        } else {
            sendMessage(event);
        }
    }

    private void sendStatus(String userID) {
        if (userID.equalsIgnoreCase(RoleID.DEV_ID)) {
            JDA jda = Repository.getJda();
            jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
                Event events = Repository.getEvent();
                Recruits recruits = Repository.getRecruits();
                events.sendInfo(privateChannel);
                recruits.sendInfo(privateChannel);
            });
        }
    }

    /**
     * @param event Wydarzenie wpisania wiadomości na prywatnym kanale
     * @return Zwraca true jeżeli użytkownik ma rolę CLAN MEMBER, w innym przypadku zwraca false.
     */
    private boolean isClanMember(PrivateMessageReceivedEvent event) {
        List<Guild> guilds = event.getJDA().getGuilds();
        for (Guild guild : guilds) {
            if (guild.getId().equalsIgnoreCase(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
                List<Member> members = guild.getMembers();
                for (Member member : members) {
                    if (member.getId().equalsIgnoreCase(event.getAuthor().getId())) {
                        List<Role> roles = member.getRoles();
                        for (Role role : roles) {
                            if (role.getId().equalsIgnoreCase(RoleID.CLAN_MEMBER_ID)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param message Wiadomość wpisana przez użytkownika
     * @return Zwraca nazwę wpisaną przez użytkownika.
     */
    private String getNewChannelNameFromMsg(String[] message) {
        String result = "";
        for (int i = 1; i < message.length; i++) {
            result += message[i] + " ";
        }
        return result;
    }

    private void sendMessage(@NotNull PrivateMessageReceivedEvent event) {
        event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. Jeżeli potrzebujesz pomocy. Wpisz **!help**").queue();
            });
        });
    }
}



