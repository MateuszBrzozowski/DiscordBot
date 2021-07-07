package events;


import embed.*;
import helpers.CategoryAndChannelID;
import helpers.Commands;
import helpers.RoleID;
import model.Event;
import model.EventsGeneratorModel;
import model.Recruits;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WriteListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        boolean radKlan = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(),RoleID.RADA_KLANU);
        boolean clanMember = RoleID.isRoleMessageRecived(event.getMessage().getMember().getRoles(),RoleID.CLAN_MEMBER_ID);
        Event matches = RangerBot.getMatches();
        Recruits recruits = RangerBot.getRecruits();

        if (message.length == 1 && message[0].equalsIgnoreCase(Commands.START_REKRUT)) {
            event.getMessage().delete().submit();
            if (radKlan) new Recruiter(event);
        }
        else if (event.getChannel().getId().equalsIgnoreCase(CategoryAndChannelID.RANGER_BOT_LOGGER)){
            new EmbedNoWriteOnLoggerChannel(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.NEGATIVE)) {
            event.getMessage().delete().submit();
            if (radKlan) new EmbedNegative(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.POSITIVE)) {
            event.getMessage().delete().submit();
            if (radKlan) new EmbedPositive(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.GENERATOR)){
            event.getMessage().delete().submit();
            if (clanMember) {
                EventsGeneratorModel eventsGeneratorModel = RangerBot.getEventsGeneratorModel();
                String authorID = event.getAuthor().getId();
                if (eventsGeneratorModel.userHaveActiveGenerator(authorID)==-1){
                    EventsGenerator eventsGenerator = new EventsGenerator(event);
                    eventsGeneratorModel.addEventsGenerator(eventsGenerator);
                }
                else {
                    new EmbedYouHaveActiveEventGenerator(event);
                }

            }
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom3Data(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom4Data(message, event);
        }
        else if (message.length == 4 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom3DataHere(message, event);
        }
        else if (message.length == 5 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)) {
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFrom4DataHere(message, event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT)){
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFromSpecificData(message,event);
        }
        else if (message.length>=7 && message[0].equalsIgnoreCase(Commands.NEW_EVENT_HERE)){
            event.getMessage().delete().submit();
            if (clanMember) matches.createNewEventFromSpecificData(message,event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.CLOSE)) {
            event.getMessage().delete().submit();
            if (radKlan) recruits.closeChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.REOPEN)) {
            event.getMessage().delete().submit();
            if (radKlan) recruits.reOpenChannel(event);
        }
        else if (message.length==1 && message[0].equalsIgnoreCase(Commands.HELPS)){
            event.getMessage().delete().submit();
            if (radKlan) new EmbedHelp(event);
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.REMOVE_CHANNEL)) {
            event.getMessage().delete().submit();
            if (radKlan) {
                logger.info("Usuwanie kanału.");
                String channelID = event.getChannel().getId();
                if (recruits.isRecruitChannel(channelID)){
                    recruits.deleteChannel(event);
                }else if (matches.isActiveMatchChannelID(channelID)>=0){
                    matches.deleteChannel(event);
                }
            }
        }
        else if (message.length == 1 && message[0].equalsIgnoreCase(Commands.DICE)){
            event.getMessage().delete().submit();
            new EmbedDice(event);
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {

        //TODO osobna klasa gdzie będą listy z wszystkimi uzytkownikami driscorda z dana rola
        //TODO można spróbowac lokalnie najpierw w tej klasie spróbować
        EventsGeneratorModel eventsGeneratorModel = RangerBot.getEventsGeneratorModel();
//        boolean isClanMember = isClanMember(event);
        if (!event.getAuthor().isBot()) {
            int indexOfGenerator = eventsGeneratorModel.userHaveActiveGenerator(event.getAuthor().getId());
            if (indexOfGenerator >= 0) {
                if (event.getMessage().getContentDisplay().equalsIgnoreCase("!cancel")) {
                    eventsGeneratorModel.cancelEventGenerator(event);
                    eventsGeneratorModel.removeGenerator(indexOfGenerator);
                } else eventsGeneratorModel.saveAnswerAndNextStage(event, indexOfGenerator);
            } else {
                sendMessage(event);
                logger.info("User nie ma aktywnego generatora");

            }
        }
    }

    private boolean isClanMember(PrivateMessageReceivedEvent event) {
        return false;
    }

    private void sendMessage(PrivateMessageReceivedEvent event) {
        String[] msg = {"Nie rozumiem","O co Ci chodzi?","Niestety, nie potrafię Cię zrozumieć!","Słucham?","Ale że jak?","Co?",
        "Ja dopiero rosnę na sile. Na chwilę obecną nie rozumiem Ciebie. Przepraszam","Chcesz bana?","Proszę mnie nie drażnić!",
        "Proszę mnie nie denerwować! Ja jestem malutki. Nie rozumiem Ciebie!","Dlaczego mi to reboisz? Zostaw mnie w spokoju",
        "Idź i nie pisz do mnie. BRZOZAAAA!!!! ratuj","Bóg jest odpowiedzią na wszystkie pytania","Stanę przed wejście do burdelu, żeby złapać w nią twoją mamuśkę",
        "Gdybyś był moim mężem, wsypałabym ci truciznę do herbaty.", "Jak nazywa się człowiek nóż? Janusz!", "Po co dresiiarz idzie do lasu? Poziomki."};
        Random random = new Random();
        event.getJDA().retrieveUserById(event.getMessage().getAuthor().getId()).queue(user -> {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(msg[random.nextInt(msg.length)]).queue();
            });
        });
    }



}



