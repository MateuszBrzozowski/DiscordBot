package stats;

public class Maps {

    private final String name;
    private int count;
    private float percentage;

    public Maps(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public float getPercentage() {
        return percentage;
    }
}
