package pl.mbrzozowski.ranger;

import net.dv8tion.jda.api.JDA;

public class Repository {

    private static JDA jda;

    public static JDA getJda() {
        return jda;
    }

    static void setJDA(JDA j) {
        jda = j;
    }

}
