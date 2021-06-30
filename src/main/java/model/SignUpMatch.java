package model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SignUpMatch {

    private List<ActiveMatch> activeSignUpMatches = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String CATEGORY_ID = "842886351346860112"; //kategoria Brzoza i Ranger testujo
    private final String CLAN_MEMBER_ID = "311978154291888141";
    private static final String NAME_LIST = ":white_check_mark: Lista ";
    private static final String NAME_LIST_RESERVE = ":wc: Rezerwa ";


    public void createSignUpList3Data(String[] message, GuildMessageReceivedEvent event) {
        event.getMessage().delete().complete();
        List<Category> categories = event.getGuild().getCategories();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CATEGORY_ID)) {
                event.getGuild().createTextChannel(message[1] + "-" + message[2] + "-" + message[3], cat).queue(textChannel -> {
                    textChannel.sendMessage("<@" + "Clan Member" + "> Zapisy!").queue();
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.setColor(Color.YELLOW);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setTitle(message[1]);
                    builder.addField(":date: Kiedy", message[2], true);
                    builder.addBlankField(true);
                    builder.addField(":clock930: Godzina", message[3], true);
                    builder.addBlankField(false);
                    builder.addField(NAME_LIST+"(0)", ">>> -", true);
                    builder.addBlankField(true);
                    builder.addField(NAME_LIST_RESERVE+"(0)", ">>> -", true);
                    builder.setFooter("Utworzony przez " + event.getMember().getNickname());
                    textChannel.sendMessage(builder.build()).setActionRow(
                                    Button.primary("in_"+textChannel.getId(), "Zapisz"),
                                    Button.secondary("reserve_"+textChannel.getId(), "Zapisz na rezerwę"),
                                    Button.danger("out_"+textChannel.getId(), "Wypisz")).queue();
                    ActiveMatch match = new ActiveMatch("in_"+textChannel.getId(),"reserve_"+textChannel.getId(),"out_"+textChannel.getId(),textChannel.getId());
                    activeSignUpMatches.add(match);
                });
                break;
            }
        }
    }

    public void updateEmbed(@NotNull ButtonClickEvent event, int indexOfMatch){
        String messageID = event.getMessage().getId();
        event.getChannel().retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed mOld = embeds.get(0);
            List<MessageEmbed.Field> fieldsOld = embeds.get(0).getFields();
            List<MessageEmbed.Field> fieldsNew = new ArrayList<>();
            String mainList = activeSignUpMatches.get(indexOfMatch).getStringOfMainList();
            String reserveList =activeSignUpMatches.get(indexOfMatch).getStringOfReserveList();

            for (int i = 0; i < fieldsOld.size(); i++) {
                String nameField = fieldsOld.get(i).getName();
                if (i==4){
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST+"("+activeSignUpMatches.get(indexOfMatch).getMainList().size()+")",">>> "+mainList,true);
                    fieldsNew.add(fieldNew);
                }
                else if (i==6){
                    MessageEmbed.Field fieldNew = new MessageEmbed.Field(NAME_LIST_RESERVE + "(" + activeSignUpMatches.get(indexOfMatch).getReserveList().size() + ")", ">>> "+reserveList, true);
                    fieldsNew.add(fieldNew);
                }else {
                    fieldsNew.add(fieldsOld.get(i));
                }
            }

            MessageEmbed m = new MessageEmbed(mOld.getUrl()
                    ,mOld.getTitle()
                    ,mOld.getDescription()
                    ,mOld.getType()
                    ,mOld.getTimestamp()
                    ,mOld.getColorRaw()
                    ,mOld.getThumbnail()
                    ,mOld.getSiteProvider()
                    ,mOld.getAuthor()
                    ,mOld.getVideoInfo()
                    ,mOld.getFooter()
                    ,mOld.getImage()
                    ,fieldsNew);
            message.editMessage(m).queue();

        });
    }

    public int isActiveMatch(String channelID){
        for (int i = 0; i < activeSignUpMatches.size(); i++) {
            if (channelID.equalsIgnoreCase(activeSignUpMatches.get(i).getChannelID())){
                return i;
            }
        }
        return -1;
    }

    public void signIn(ButtonClickEvent event, int indexOfActiveMatch) {
        String userName = event.getMember().getNickname();
        String userID = event.getUser().getId();
        activeSignUpMatches.get(indexOfActiveMatch).addToMainList(userID,userName,event);

    }

    public void signINReserve(ButtonClickEvent event, int indexOfMatch) {
        String userName = event.getMember().getNickname();
        String userID = event.getUser().getId();
        activeSignUpMatches.get(indexOfMatch).addToReserveList(userID,userName,event);
    }

    public void signOut(ButtonClickEvent event, int indexOfMatch) {
        String userID = event.getUser().getId();
        activeSignUpMatches.get(indexOfMatch).removeFromMatch(userID);
    }
}
