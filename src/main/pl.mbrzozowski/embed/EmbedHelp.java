package embed;

import helpers.CategoryAndChannelID;
import helpers.RangerLogger;
import helpers.RoleID;
import helpers.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

import java.awt.*;
import java.util.List;

public class EmbedHelp {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EmbedHelp(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builderRecrut = new EmbedBuilder();
            builderRecrut.setTitle("Ranger Bot - POMOC");
            builderRecrut.setFooter("RangerBot created by Brzozaaa © 2021");
            builderRecrut.setThumbnail(EmbedSettings.THUMBNAIL);
            builderRecrut.setColor(Color.YELLOW);
            builderRecrut.addField("REKRUCI", "\n>>> **!p** - Pinguje rekruta i wysyła na kanale POZYTYWNY wynik rekrutacji\n" +
                    "**!n** - Pinguje rekruta i wysyła na kanale NEGATYWNY wynik rekrutacji\n" +
                    "**!close** - Zamyka kanał rekrutacji - rekrut nie widzi kanału/nie może pisać.\n" +
                    "**!open** - Otwiera kanał rekrutacji - rekrut ponownie może widzieć i pisać na kanale.\n" +
                    "**!remove** - Usuwa kanał rekrutacji. Możesz usunąć kanał ręcznie bez komendy.\n\n" +
                    "", false);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle("Ranger Bot - POMOC");
            builder.setFooter("RangerBot created by Brzozaaa © 2021");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            builder.addField("ZAPISY/LISTA podstawowa", "\n>>> **!zapisy <nazwa> <data> <godzina>** - tworzy kanał i na nim listę.\n**!zapisyhere <nazwa> <data> <godzina>** - tworzy listę na kanale na którym się znajdujemy. " +
                    "Użyteczne przy tworzeniu w pierwszej kolejności kanału dla eventu, a potem jeżeli chcemy dodać listę. (Nowy kanał) \n\n " +
                    "Polecenie !zapisy wpisujemy na dowolnym kanale lub w prywatnej wiadomości do bota.\n" +
                    "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                    "Nazwa eventu jednoczłonowa bez opisu zawartego na liście. \n**UWAGA** Kolejność parametrów ma znaczenie! \n" +
                    "Dodatkowo możemy dodać tylko jeden parametr na końcu komendy.\n" +
                    "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                    "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                    "defaultowo widzi i pinguje Clan Memberów.\n\n" +
                    "(przykład: !zapisy CCFN 19.06.2021 19:30)\n" +
                    "(przykład: !zapisy CCFN 19.06.2021 19:30 -ac)\n" +
                    "(przykład: !zapisy CCFN 19.06.2021 19:30 -r)\n\n", false);
            builder.addField("ZAPISY/LISTA zaawansowana z tworzeniem kanału", "\n>>> **!zapisy \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                    "Polecenie wpisujemy na dowolnym kanale lub w prywatnej wiadomości do bota.\n" +
                    "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                    "Otwiera nowy kanał, tworzy listę. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                    "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                    "Dodatkowe parametry:\n" +
                    "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                    "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                    "Defaultowo widzi i pinguje Clan Memberów.\n" +
                    "Maksymalna liczba znaków:\n" +
                    "Nazwa eventu - 256\n" +
                    "Tekst (opis eventu) - 2048 - Jeżeli chcesz wpisać własnoręcznie lub twój opis będzie dłuższy patrz **Nowy kanał**\n\n" +
                    "(przykład: !zapisy -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);
            builder.addField("ZAPISY/LISTA zaawansowana na kanale.", "\n>>> **!zapisyhere \n-name <nazwa> \n-date <data> \n-time <czas> \n-o <opis>** \n\n " +
                    "Format daty: dd.MM.yyyy; Format czasu: hh:mm.\n" +
                    "Tworzy listę na mecz na kanale na którym się znajdujemy. Używamy gdy nazwa eventu składa się więcej niż z jendego wyrazu " +
                    "lub chcemy dodać krótki opis eventu zawarty w na liscie\n" +
                    "Dodatkowe parametry:\n" +
                    "**-ac** kanał widoczny i ping dla ClanMember i Rekrut\n" +
                    "**-r** kanał widoczny dla Clan Member i rekrut, Pinguje tylko rekrutów.\n" +
                    "**-c** kanał widoczny dla Clan Member, Pinguje tylko Clan Memberów.\n" +
                    "defaultowo lista tworzy się bez pingowania żadnej roli nie nadpisując uprawnień kanału.\n" +
                    "Maksymalna liczba znaków:\n" +
                    "Nazwa eventu - 256\n" +
                    "Tekst (opis eventu) - 2048\n\n" +
                    "(przykład: !zapisyhere -name Event testowy -date 19.06.2021 -time 19:30 -o opis eventu -ac)\n\n", false);
            builder.addField("Nowy kanał", ">>> **!newChannel** - Tworzy nowy kanał widoczny tylko dla Ciebie.\n" +
                    "**!name <nazwa>** - wpisz na nowo utworzonym kanale aby zmienić nazwe kanału (najlepiej nazwa eventu)\n\n" +
                    "Następnie wpisz swój opis i użyj komendy !zapisyhere z odpowiednimi parametrami (patrz. wyżej) lub !generatorhere", false);
            EmbedBuilder builderGenerator = new EmbedBuilder();
            builderGenerator.setTitle("Ranger Bot - POMOC");
            builderGenerator.setFooter("RangerBot created by Brzozaaa © 2021");
            builderGenerator.setThumbnail(EmbedSettings.THUMBNAIL);
            builderGenerator.setColor(Color.YELLOW);
            builderGenerator.addField("Genereator", ">>> **!generator** - Tworzy kanał i listę w sekcji mecze/szkolenia/eventy\n" +
                    "(Polecenie możesz napisać tutaj w prywatnej wiadomości lub na dowolnym kanale.)\n" +
                    "**!generatorHere** - Tworzy listę na kanale w którym polecenie zostalo wpisane. Użyteczne przy tworzeniu w pierwszej kolejności kanału dla eventu, " +
                    "a potem jeżeli chcemy dodać listę. (POMOC - Nowy kanał)\n\n" +
                    "Po wpisaniu powyższych komend uruchamia się generator eventu. Postępuj zgodnie z instrukcjami.", false);

            EmbedBuilder builderDice = new EmbedBuilder();
            builderDice.setTitle("Ranger Bot - POMOC");
            builderDice.setFooter("RangerBot created by Brzozaaa © 2021");
            builderDice.setThumbnail(EmbedSettings.THUMBNAIL);
            builderDice.setColor(Color.YELLOW);
            builderDice.addField("Gry", ">>> **!kostka** - losuje i wyświetla wylosowną liczbę.\n" +
                    "**!kostka <Temat_gry>** - Rozpoczyna grę na kanale na którym zostało wpisane polecenie. Gra na dwie osoby. Osoba z większą liczbą wygrywa.", false);

            EmbedBuilder builderDev = new EmbedBuilder();
            builderDev.setTitle("Ranger Bot - POMOC - DEV");
            builderDev.setColor(Color.RED);
            builderDev.addField("Komendy DEV", "**!time [msgID] [HH:mm]** - zmienia godzine w evencie\n" +
                    "**!date [msgID] [dd.MM.yyyy]** - zmienia date w evencie\n" +
                    "**!disable [msgID]** -  Wyłącza buttony\n" +
                    "**!enable [msgID]** - Włącza buttony\n" +
                    "**!deleteEvent [msgID]** - zamyka event i usuwa event z bazy danych.\n" +
                    "**!status** - Wyswietla status aplikacji.", false);

            if (Users.hasUserRole(userID, RoleID.RADA_KLANU)) privateChannel.sendMessage(builderRecrut.build()).queue();
            privateChannel.sendMessage(builderGenerator.build()).queue();
            privateChannel.sendMessage(builder.build()).queue();
            privateChannel.sendMessage(builderDice.build()).queue();
            if (userID.equalsIgnoreCase("642402714382237716"))
                privateChannel.sendMessage(builderDev.build()).queue();

            RangerLogger.info("Użytkownik [" + Users.getUserNicknameFromID(userID) + "] poprosił o pomoc. Ranger Bot Pomoc została wyświetlona.");
        });
    }
}
