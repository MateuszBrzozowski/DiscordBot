package model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.List;

public class SignUpMatch {


    public void createSignUpList(String[] message, GuildMessageReceivedEvent event) {
        String nameCategory = "Brzoza i Ranger testujo";
        List<Category> categories = event.getGuild().getCategories();
        String idClanMember = event.getGuild().getRolesByName("Clan Member", true).get(0).getId();
        for (Category cat : categories) {
            if (cat.getName().equalsIgnoreCase(nameCategory)) {
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
                    //TODO id danego meczu
                    textChannel.sendMessage(builder.build()).setActionRow(net.dv8tion.jda.api.interactions.components.Button.primary("in_", "Zapisz"), net.dv8tion.jda.api.interactions.components.Button.secondary("reserve_", "Zapisz na rezerwę"), Button.danger("out_", "Wypisz")).queue();
                });
            }
        }
    }
}
