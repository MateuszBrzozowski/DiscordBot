package stats;

public class MapWithCountStatistic extends Maps {

    private final int count;
    private float percentage;

    public MapWithCountStatistic(String name, int count) {
        super(name);
        this.count = count;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getCount() {
        return count;
    }

    public float getPercentage() {
        return percentage;
    }
}
