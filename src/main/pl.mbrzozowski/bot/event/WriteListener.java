package bot.event;


import bot.event.writing.*;
import embed.EmbedHelp;
import event.Event;
import event.EventsGeneratorModel;
import helpers.Commands;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

//    public static void main(String[] args) {
//        Message msg = new Message(new String[]{"!test"}, "!test", RoleID.DEV_ID);
//        Proccess cmdRecrut = new RecrutCmd();
//        Proccess cmdEventss = new EventsCmd();
//
//        cmdRecrut.setNextProccess(cmdEventss);
//        cmdRecrut.proccessMessage(msg);
//    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        String contentDisplay = event.getMessage().getContentDisplay();

        Message msg = new Message(message, contentDisplay, event.getAuthor().getId());


        DiceCmd diceCmd = new DiceCmd(event);
        CheckUser checkUser = new CheckUser();
        LogChannel logChannel = new LogChannel(event);
        GeneratorCmd generatorCmd = new GeneratorCmd(event);
        EventsCmd eventsCmd = new EventsCmd(event);
        ChannelCmd channelCmd = new ChannelCmd(event);
        CheckUserAdmin checkUserAdmin = new CheckUserAdmin();
        HelpCmd helpCmd = new HelpCmd(event);
        RecrutCmd recrutCmd = new RecrutCmd(event);

        diceCmd.setNextProccess(logChannel);
        logChannel.setNextProccess(checkUser);
        checkUser.setNextProccess(generatorCmd);
        generatorCmd.setNextProccess(eventsCmd);
        eventsCmd.setNextProccess(channelCmd);
        channelCmd.setNextProccess(helpCmd);
        helpCmd.setNextProccess(checkUserAdmin);
        checkUserAdmin.setNextProccess(recrutCmd);

        diceCmd.proccessMessage(msg);


//        if (clanMember) {
//            if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
//                event.getMessage().delete().submit();
//                if (admin) new Recruiter(event);
//            } else if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.RANGER_BOT_LOGGER)) {
//                EmbedInfo.noWriteOnLoggerChannel(event);
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATIVE)) {
//                event.getMessage().delete().submit();
//                if (admin) EmbedInfo.endNegative(event.getAuthor().getId(), event.getChannel());
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POSITIVE)) {
//                event.getMessage().delete().submit();
//                if (admin) EmbedInfo.endPositive(event.getAuthor().getId(), event.getChannel());
//            } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ACCEPT_RECRUT)) {
//                event.getMessage().delete().submit();
//                if (admin) recruits.acceptRecrut(message[1], event.getChannel(), event.getAuthor());
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.GENERATOR)) {
//                event.getMessage().delete().submit();
//                EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
//                String authorID = event.getAuthor().getId();
//                int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
//                if (indexOfGenerator == -1) {
//                    EventsGenerator eventsGenerator = new EventsGenerator(event);
//                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//                } else {
//                    EmbedInfo.userHaveActiveEventGenerator(event.getAuthor().getId());
//                    eventsGeneratorModel.cancelEventGenerator(event);
//                    EmbedInfo.createNewGenerator(event.getAuthor().getId());
//                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
//                    EventsGenerator eventsGenerator = new EventsGenerator(event);
//                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//                }
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.GENERATOR_HERE)) {
//                event.getMessage().delete().submit();
//                EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
//                String authorID = event.getAuthor().getId();
//                int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(authorID);
//                if (indexOfGenerator == -1) {
//                    EventsGenerator eventsGenerator = new EventsGenerator(event);
//                    eventsGenerator.setHere(true);
//                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//                } else {
//                    EmbedInfo.userHaveActiveEventGenerator(event.getAuthor().getId());
//                    eventsGeneratorModel.cancelEventGenerator(event);
//                    EmbedInfo.createNewGenerator(event.getAuthor().getId());
//                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
//                    EventsGenerator eventsGenerator = new EventsGenerator(event);
//                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//                }
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
//                event.getMessage().delete().submit();
//                String userID = event.getMessage().getAuthor().getId();
//                matches.createNewChannel(event.getGuild(), userID);
//            } else if (message.length > 1 && message.length < 100 && message[0].equalsIgnoreCase(Commands.NAME)) {
//                if (matches.checkChannelIsInEventCategory(event)) {
//                    String name = getNewChannelNameFromMsg(message);
//                    event.getMessage().delete().submit();
//                    event.getChannel().getManager().setName(name).queue();
//                }
//            } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFrom3Data(message, event);
//            } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFrom4Data(message, event);
//            } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFrom3DataHere(message, event);
//            } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFrom4DataHere(message, event);
//            } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFromSpecificData(message, event);
//            } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
//                event.getMessage().delete().submit();
//                matches.createNewEventFromSpecificData(message, event);
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.CLOSE)) {
//                event.getMessage().delete().submit();
//                if (admin) recruits.closeChannel(event);
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REOPEN)) {
//                event.getMessage().delete().submit();
//                if (admin) recruits.reOpenChannel(event);
//            } else if (message.length >= 1 && message[0].equalsIgnoreCase(Commands.HELPS)) {
//                event.getMessage().delete().submit();
//                EmbedHelp.help(event.getMessage().getAuthor().getId(), message);
//            } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
//                event.getMessage().delete().submit();
//                if (admin) {
//                    logger.info("Usuwanie kanału.");
//                    String channelID = event.getChannel().getId();
//                    if (recruits.isRecruitChannel(channelID)) {
//                        recruits.deleteChannel(event);
//                    } else if (matches.isActiveMatchChannelID(channelID) >= 0) {
//                        matches.deleteChannel(event);
//                    }
//                }
//            }
//            }

//        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.DICE)) {
//            event.getMessage().delete().submit();
//            DiceGame diceGame = new DiceGame();
//            diceGame.playSolo(event);
//        } else if (message.length > 1 && message[0].equalsIgnoreCase(Commands.DICE)) {
//            event.getMessage().delete().submit();
//            DiceGame diceGame = new DiceGame(message, event);
//            DiceGames diceGames = Repository.getDiceGames();
//            diceGames.addGame(diceGame);
//        } else {
//            DiceGames diceGames = Repository.getDiceGames();
//            if (!event.getAuthor().isBot()) {
//                if (diceGames.isActiveGameOnChannelID(event.getChannel().getId())) {
//                    event.getMessage().delete().submit();
//                    diceGames.play(event);
//                }
//            }
//        }
//    }

    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        boolean admin = Users.hasUserRole(event.getAuthor().getId(), RoleID.RADA_KLANU);
        if (!admin) admin = Users.isUserDev(event.getAuthor().getId());
        boolean clanMember = Users.hasUserRole(event.getAuthor().getId(), RoleID.CLAN_MEMBER_ID);

        if (!clanMember) return;

        String[] message = event.getMessage().getContentRaw().split(" ");
        Event matches = Repository.getEvent();

        EventsGeneratorModel eventsGeneratorModel = Repository.getEventsGeneratorModel();
        int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(event.getAuthor().getId());

//        if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ACCEPT_RECRUT)) {
//            Recruits recruits = RangerBot.getRecruits();
//            recruits.acceptRecrut(message[1], event.getChannel(), event.getAuthor());
//        }

//        if (event.getMessage().getContentDisplay().equalsIgnoreCase(Commands.GENERATOR)) {
//            String authorID = event.getAuthor().getId();
//            if (eventsGeneratorModel.userHaveActiveGenerator(authorID) == -1) {
//                EventsGenerator eventsGenerator = new EventsGenerator(event);
//                eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//            } else {
//                EmbedInfo.userHaveActiveEventGenerator(event.getAuthor().getId());
//                eventsGeneratorModel.cancelEventGenerator(event);
//                EmbedInfo.createNewGenerator(event.getAuthor().getId());
//                eventsGeneratorModel.removeGenerator(indexOfGenerator);
//                EventsGenerator eventsGenerator = new EventsGenerator(event);
//                eventsGeneratorModel.addEventsGenerator(eventsGenerator);
//            }
//        }
//        if (message.length >= 1 && message[0].equalsIgnoreCase(Commands.HELPS)) {
//            EmbedHelp.help(event.getMessage().getAuthor().getId(), message);
//        } else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//            matches.createNewEventFrom3Data(message, event);
//        } else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//            matches.createNewEventFrom4Data(message, event);
//        } else if (message.length >= 7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
//            matches.createNewEventFromSpecificData(message, event);
//        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEW_CHANNEL)) {
//            String userID = event.getMessage().getAuthor().getId();
//            matches.createNewChannel(event, userID);
//        } else if (indexOfGenerator >= 0) {
//            if (event.getMessage().getContentDisplay().equalsIgnoreCase("!cancel")) {
//                eventsGeneratorModel.cancelEventGenerator(event);
//                eventsGeneratorModel.removeGenerator(indexOfGenerator);
//            } else eventsGeneratorModel.saveAnswerAndNextStage(event, indexOfGenerator);
//        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.DELETE_EVENT)) {
//            if (admin) matches.removeEvent(message[1]);
//        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
//            if (admin) matches.disableButtons(message[1]);
//        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.DISABLE_BUTTONS)) {
//            if (admin) matches.disableButtons(message[1], message[2]);
//        } else if (message.length == 2 && message[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
//            if (admin) matches.enableButtons(message[1]);
//        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.ENABLE_BUTTONS)) {
//            if (admin) matches.enableButtons(message[1], message[2]);
//        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.TIME)) {
//            if (admin) matches.changeTime(message[1], message[2], event.getAuthor().getId());
//        } else if (message.length == 3 && message[0].equalsIgnoreCase(Commands.DATE)) {
//            if (admin) matches.changeDate(message[1], message[2], event.getAuthor().getId());
//        } else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.STATUS)) {
//            sendStatus(event.getAuthor().getId());
//        } else {
//            sendMessage(event);
//        }
    }

//    /**
//     * @param message Wiadomość wpisana przez użytkownika
//     * @return Zwraca nazwę wpisaną przez użytkownika.
//     */
//    private String getNewChannelNameFromMsg(String[] message) {
//        String result = "";
//        for (int i = 1; i < message.length; i++) {
//            result += message[i] + " ";
//        }
//        return result;
//    }

//    private void sendMessage(@NotNull PrivateMessageReceivedEvent event) {
//        event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
//            user.openPrivateChannel().queue(privateChannel -> {
//                privateChannel.sendMessage("Niestety, nie rozumiem Ciebie. Jeżeli potrzebujesz pomocy. Wpisz **!help**").queue();
//            });
//        });
//    }
}



