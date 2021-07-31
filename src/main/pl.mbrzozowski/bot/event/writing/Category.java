package bot.event.writing;

import event.Event;
import ranger.Repository;
import recrut.Recruits;
import recrut.Recrut;

public abstract class Category {

    private Recruits recruits = Repository.getRecruits();
    private Event events = Repository.getEvent();
    private Category nextCategory;

    abstract void proccessMessage(Message message);

    public void setNextCategory(Category nextCategory) {
        this.nextCategory = nextCategory;
    }

    protected Category getNextCategory() {
        return nextCategory;
    }

    public Recruits getRecruits() {
        return recruits;
    }

    public Event getEvents() {
        return events;
    }
}
