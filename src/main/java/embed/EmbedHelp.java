package embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class EmbedHelp {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedHelp(GuildMessageReceivedEvent event) {
        String userID = event.getMessage().getAuthor().getId();
        event.getJDA().retrieveUserById(userID).queue(user -> {
            event.getGuild().retrieveMemberById(user.getId()).queue(member -> {
                logger.info("Uzytkownik {} poprosił o pomoc.", member.getNickname());
                user.openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.YELLOW);
                    builder.setTitle("Ranger Bot - POMOC");
                    builder.addField("REKRUCI", "\n>>> **!p** - Pinguje rekruta i wysyła na kanale POZYTYWNY wynik rekrutacji\n" +
                            "**!n** - Pinguje rekruta i wysyła na kanale NEGATYWNY wynik rekrutacji\n" +
                            "**!close** - Zamyka kanał rekrutacji - rekrut nie widzi kanału/nie może pisać.\n" +
                            "**!open** - Otwiera kanał rekrutacji - rekrut ponownie może widzieć i pisać na kanale.\n" +
                            "**!remove** - Usuwa kanał rekrutacji. Możesz usunąć kanał ręcznie bez komendy.\n\n" +
                            "", false);
                    builder.setFooter("RangerBot created by Brzozaaa © 2021");
                    builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                    builder.addField("ZAPISY/LISTA podstawowa", "\n>>> **!zapisy <nazwa> <data> <godzina>** - tworzy kanał i na nim listę.\n**!zapisyhere <nazwa> <data> <godzina>** - tworzy listę na kanale na którym się znajdujemy. \n\n " +
                            "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                            "Nazwa eventu jednoczłonowa bez opisu zawartego na liście. \n**UWAGA** Kolejność parametrów ma znaczenie! \n" +
                            "Możemy dodać tylko jeden parametr na końcu komendy.\n" +
                            "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                            "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                            "defaultowo widzi i pinguje Clan Memberów.\n\n" +
                            "(przykład: !zapisy CCFN 19.06.2021 19:30)\n" +
                            "(przykład: !zapisy CCFN 19.06.2021 19:30 -ac)\n" +
                            "(przykład: !zapisy CCFN 19.06.2021 19:30 -r)\n\n", false);
                    builder.addField("ZAPISY/LISTA zaawansowana z tworzeniem kanału", "\n>>> **!zapisy \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                            "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                            "Otwiera nowy kanał, tworzy listę. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                            "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                            "Dodatkowe parametry:\n" +
                            "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                            "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                            "Defaultowo widzi i pinguje Clan Memberów.\n" +
                            "Maksymalna liczba znaków:\n" +
                            "Nazwa eventu - 256\n" +
                            "Tekst (opis eventu) - 2048\n\n" +
                            "(przykład: !zapisy -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);
                    builder.addField("ZAPISY/LISTA zaawansowana na kanale.", "\n>>> **!zapisyhere \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                            "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                            "Tworzy listę na mecz na kanale na którym się znajdujemy. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                            "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                            "Dodatkowe parametry:\n" +
                            "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                            "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                            "**-c** kanał widoczny dla Clan Member, Pinguje tylko Clan Memberów.\n" +
                            "defaultowo lista tworzy się bez pingowania żadnej roli nie nadpisując ustawień kanału.\n" +
                            "Maksymalna liczba znaków:\n" +
                            "Nazwa eventu - 256\n" +
                            "Tekst (opis eventu) - 2048\n\n" +
                            "(przykład: !zapisyhere -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);

                    privateChannel.sendMessage(builder.build()).queue();
                    logger.info("Wiadomość prywatna z pomocą wysłana.");
                });
            });
        });
    }
}
