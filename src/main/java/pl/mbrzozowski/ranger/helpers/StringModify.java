package pl.mbrzozowski.ranger.helpers;

import org.jetbrains.annotations.NotNull;

public class StringModify {

    public static String removeDiscordMarkdowns(String source) {
        source = removeItalicsUnderscore(source);
        source = removeStar(source);
        source = removeGreaterThan(source);
        return source;
    }

    public static String removeClanTag(@NotNull String source) {
        String result = source;
        if (result.matches("(.*)<rRangersPL>(.*)")) {
            result = result.replace("<rRangersPL>", "");
        } else if (result.matches("(.*)<RangersPL>(.*)")) {
            result = result.replace("<RangersPL>", "");
        }
        return result;
    }

    private static String removeGreaterThan(String source) {
        if (source.contains(">>>")) {
            return source.replaceAll(">>>", "\\\\>>>");
        } else {
            return source.replaceAll(">", "\\\\>");
        }
    }

    private static String removeStar(String source) {
        return source.replaceAll("\\*", "\\\\*");
    }

    private static String removeItalicsUnderscore(@NotNull String source) {
        return source.replaceAll("_", "\\\\_");
    }


}
