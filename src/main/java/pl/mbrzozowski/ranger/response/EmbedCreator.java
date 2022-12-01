package pl.mbrzozowski.ranger.response;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedCreator {

    private static final String footer = "ranger.RangerBot created by Brzozaaa © 2022";

    /**
     * Created and returns a new Embedbuilder
     * <p>
     * WARNING = red + warning thumbnail</p>
     * <p>
     * INF_CONFIRM = only Green color</p>
     * <p>
     * INF_RED = only red color</p>
     * <p>
     * QUESTION = only Yellow color
     * </p>
     * <p>DEFAULT = Yellow color and thumbnail default (logo)</p>
     *
     * @param style of Embed
     * @return Enbed builder with color and Thumbnail
     */
    public static EmbedBuilder getEmbedBuilder(EmbedInfo.EmbedStyle style) {
        return switch (style) {
            case DEFAULT_HELP -> getEmbedBuilder(Color.YELLOW, ThumbnailType.DEFAULT, true);
            case WARNING -> getEmbedBuilder(Color.RED, ThumbnailType.WARNING);
            case INF_CONFIRM -> getEmbedBuilder(Color.GREEN);
            case INF_RED -> getEmbedBuilder(Color.RED);
            case QUESTION -> getEmbedBuilder(Color.YELLOW);
            default -> getEmbedBuilder(Color.YELLOW, ThumbnailType.DEFAULT);
        };
    }

    public static EmbedBuilder getEmbedBuilder(Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);
        return builder;
    }

    public static EmbedBuilder getEmbedBuilder(Color color, EmbedInfo.ThumbnailType thumbnailType, boolean withFooter) {
        EmbedBuilder builder = getEmbedBuilder(color, thumbnailType);
        if (withFooter) {
            builder.setFooter(getFooter());
        }
        return builder;
    }

    public static EmbedBuilder getEmbedBuilder(Color color, EmbedInfo.ThumbnailType thumbnailType) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);
        switch (thumbnailType) {
            case DEFAULT -> builder.setThumbnail(EmbedSettings.THUMBNAIL);
            case WARNING -> builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
            case DICE -> builder.setThumbnail(EmbedSettings.THUMBNAIL_DICE);
        }
        return builder;
    }

    public static String getFooter() {
        return footer;
    }

    public enum ThumbnailType {
        DEFAULT,
        WARNING,
        DICE
    }

    public enum EmbedStyle {
        DEFAULT,
        DEFAULT_HELP,
        WARNING,
        QUESTION,
        INF_CONFIRM,
        INF_RED,
    }
}
