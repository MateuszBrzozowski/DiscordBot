package pl.mbrzozowski.ranger.dice;

import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class DiceGame {
    private String gameName;
    private String channelID;
    private String player1Name;
    private String player2Name;

    public DiceGame(String[] gameName, @NotNull MessageReceivedEvent event) {
        this.gameName = getNameFromTable(gameName);
        this.channelID = event.getChannel().getId();
        embedInviteToGame(event);
    }

    public DiceGame() {
    }

    private void embedInviteToGame(MessageReceivedEvent event) {
        player1Name = getUserNameFromEvent(event);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail("https://www.iconsdb.com/icons/download/white/dice-64.png");
        builder.setTitle(player1Name + " zapraszą do gry w kostkę! Aby zagrać wpisz na tym kanale dowolną wiadomość.");
        if (gameName != null) {
            builder.addField("Temat gry:", gameName, false);
        }
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    private String getUserNameFromEvent(MessageReceivedEvent event) {
        String userName = event.getMessage().getMember().getNickname();
        if (userName == null) {
            userName = event.getMessage().getAuthor().getName();
        }
        return userName;
    }

    private String getNameFromTable(String[] gameName) {
        if (gameName[1].equalsIgnoreCase("null")) {
            return null;
        } else {
            String name = "";
            for (int i = 1; i < gameName.length; i++) {
                name += gameName[i] + " ";
            }
            return name;
        }
    }

    public String getChannelID() {
        return channelID;
    }

    public void play(MessageReceivedEvent event) {
        player2Name = getUserNameFromEvent(event);
        while (true) {
            int player1 = drawNumber();
            int player2 = drawNumber();
            if (player1 > player2) {
                showResult(player1Name, player1, player2Name, player2, event);
                break;
            } else if (player2 > player1) {
                showResult(player2Name, player2, player1Name, player1, event);
                break;
            }
        }
    }

    private void showResult(String playerWinName, int winInt, String playerLoseName, int loseInt, MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.WHITE);
        builder.setThumbnail("https://www.iconsdb.com/icons/download/white/dice-64.png");
        if (gameName != null) builder.setTitle(gameName);
        builder.addField(playerWinName + " wygrał z " + playerLoseName, playerWinName + " : " + winInt + "\n" +
                playerLoseName + " : " + loseInt, false);
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    private int drawNumber() {
        Random random = new Random();
        int liczba = random.nextInt(6) + 1;
        return liczba;
    }

    public void playSolo(@NotNull MessageReceivedEvent event) {
        String userName = Users.getUserNicknameFromID(event.getAuthor().getId());
        EmbedBuilder builder = new EmbedBuilder();
        int liczba = drawNumber();
        builder.setColor(Color.WHITE);
        builder.setTitle("Wylosowana liczba:");
        builder.addField(String.valueOf(liczba), "", false);
        builder.setThumbnail(EmbedSettings.THUMBNAIL_DICE);
        builder.setFooter(userName);
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }
}
