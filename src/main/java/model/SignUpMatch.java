package model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SignUpMatch {

    private List<ActiveMatch> activeSignUpMatches = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String CATEGORY_ID = "842886351346860112"; //kategoria Brzoza i Ranger testujo

    public SignUpMatch(@NotNull GuildMessageReceivedEvent event) {
        event.getMessage().delete().complete();
    }

    public void createSignUpList3Data(String[] message, GuildMessageReceivedEvent event) {
        List<Category> categories = event.getGuild().getCategories();
        String idClanMember = event.getGuild().getRolesByName("Clan Member", true).get(0).getId();
        for (Category cat : categories) {
            if (cat.getId().equalsIgnoreCase(CATEGORY_ID)) {
                event.getGuild().createTextChannel(message[1] + "-" + message[2] + "-" + message[3], cat).queue(textChannel -> {
                    textChannel.sendMessage("<@" + "Clan Member" + "> Zapisy!").queue();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.YELLOW);
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.setTitle("Lista");
                    builder.addField(message[1], "", true);
                    builder.addField("Dzień", message[2], true);
                    builder.addField("Godzina", message[3], true);
                    builder.addField("Zapisanych:", "3", false);
                    builder.addField("Lista:", "1. Rysiek z klanu\n2. Beata ta od grażyny\n3.Sama Grazyna", false);
                    builder.addField("Rezerwa", "No to dam innym", false);
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
}
