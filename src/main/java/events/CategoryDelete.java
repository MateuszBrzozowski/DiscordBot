package events;

import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryDelete extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onCategoryDelete(@NotNull CategoryDeleteEvent event) {
        logger.info(event.getCategory().getName());
        logger.info(event.getCategory().getId());
    }
}
