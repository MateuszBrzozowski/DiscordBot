package pl.mbrzozowski.ranger.stats;

public class Gun {

    private final String name;
    private int count;

    public Gun(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
