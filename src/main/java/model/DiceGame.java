package model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class DiceGame {
    private String gameName;
    private String channelID;
    private String player1Name;
    private String player2Name;

    public DiceGame(String[] gameName, @NotNull GuildMessageReceivedEvent event) {
        this.gameName = getNameFromTable(gameName);
        this.channelID = event.getChannel().getId();
        embedInviteToGame(event);
    }

    private void embedInviteToGame(GuildMessageReceivedEvent event) {
        player1Name= getUserNameFromEvent(event);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail("https://www.iconsdb.com/icons/download/white/dice-64.png");
        builder.setTitle(player1Name + " zapraszą do gry w kostkę! Aby zagrać wpisz na tym kanale dowolną wiadomość.");
        if (gameName!=null){
            builder.addField("Temat gry:", gameName,false);
        }
        event.getChannel().sendMessage(builder.build()).queue();
    }

    private String getUserNameFromEvent(GuildMessageReceivedEvent event) {
        String userName = event.getMessage().getMember().getNickname();
        if (userName==null){
            userName = event.getMessage().getAuthor().getName();
        }
        return userName;
    }

    private String getNameFromTable(String[] gameName) {
        if (gameName[1].equalsIgnoreCase("null")){
            return null;
        }
        else {
            String name = "";
            for (int i = 1; i < gameName.length; i++) {
                name+=gameName[i] + " ";
            }
            return name;
        }
    }

    public String getChannelID() {
        return channelID;
    }

    public void play(GuildMessageReceivedEvent event) {
        player2Name = getUserNameFromEvent(event);
        while (true){
            int player1 = losujLiczbę();
            int player2 = losujLiczbę();
            if (player1>player2){
                showResult(player1Name,player1,player2Name,player2,event);
                break;
            }
            else if (player2>player1){
                showResult(player2Name,player2,player1Name,player1, event);
                break;
            }
        }
    }

    private void showResult(String playerWinName, int winInt, String playerLoseName, int loseInt, GuildMessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail("https://www.iconsdb.com/icons/download/white/dice-64.png");
        if (gameName!=null) builder.setTitle(gameName);
        builder.addField(playerWinName + " wygrał z " + playerLoseName,playerWinName + " : " + winInt +"\n" +
                playerLoseName + " : " + loseInt,false);
        event.getChannel().sendMessage(builder.build()).queue();
    }

    private int losujLiczbę() {
        Random random = new Random();
        int liczba = random.nextInt(6)+1;
        return liczba;
    }
}
