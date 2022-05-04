package embed;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedServerRules {

    public static void sendServerRules(MessageReceivedEvent messageReceived) {
//        serverName(messageReceived);
        mainRulesPL(messageReceived);
        squadRulesPL(messageReceived);
        fobRulesPL(messageReceived);
        vehicleRulesPL(messageReceived);
        desciptionPL(messageReceived);
    }

    private static void serverName(MessageReceivedEvent messageReceived) {
        String msg = "**[PL/ENG] LTW - Lead The Way <RangersPL> | DiscordStatsTrack RULES**";
        messageReceived.getTextChannel().sendMessage(msg).queue();
    }

    private static void mainRulesPL(MessageReceivedEvent messageReceived) {
        String msg = "```md\n" +
                "Podstawowe zasady\n" +
                "==================\n\n" +
                "\t1.1. Żadne przejawy rasizmu, toksyczności czy dyskryminacji nie będą tolerowane. Skutkować będą kickiem, a w poważniejszych przypadkach zablokowaniem dostępu na serwer.\n" +
                "\t1.2. Zakaz propagowania komunizmu lub innego ustroju totalitarnego pod żadną postacią.\n" +
                "\t1.3. Każda forma wykorzystywania błędów gry jest zabroniona.\n" +
                "\t1.4. Zakłócanie komunikacji na czatach głosowych jest niedozwolone.\n" +
                "\t1.5. Rekrutowanie lub reklamowanie klanów bądź organizacji innych niż RangersPL jest zabronione i będzie skutkowało perm banem.\n" +
                "\t1.6. Jako że na naszym serwerze porozumiewamy się zarówno w języku polskim jak i angielskim, zaleca się stosowanie języka angielskiego na COMMAND CHAT, " +
                "przez co drużyny obcokrajowe nie będą wykluczone ze wspólnej rozgrywki.\n" +
                "\t1.7. Każdy gracz musi mieć nazwę możliwą do wymówienia, składającą się z liter łacińskich ew. cyfr arabskich.\n" +
                "\t1.8. Dzielenie się istotnymi dla gry informacjami z przeciwną drużyną jest zabronione.\n" +
                "\t1.9. Streamowanie rozgrywki jest dozwolone z opóźnieniem nie mniejszym niż 10 minut.\n" +
                "\t1.10. Zabrania się uniemożliwiania wrogim jednostką opuszczenia głównej bazy.\n" +
                "\t1.11. Nie strzelaj do swoich, jeśli przez przypadek Ci się zdarzy, przeproś na czacie ogólnym." +
                "```";
        messageReceived.getTextChannel().sendMessage(msg).queue();
    }

    private static void squadRulesPL(MessageReceivedEvent messageReceived) {

    }

    private static void fobRulesPL(MessageReceivedEvent messageReceived) {

    }

    private static void vehicleRulesPL(MessageReceivedEvent messageReceived) {

    }

    private static void desciptionPL(MessageReceivedEvent messageReceived) {

    }
}
