package pl.mbrzozowski.ranger.helpers;

public class Math {

    public static int roundTo5(int value) {
        if (value % 5 == 0) {
            return value;
        } else if (value > 0) {
            if (value % 5 <= 2) {
                return value - (value % 5);
            } else {
                return value - (value % 5) + 5;
            }
        } else {
            return roundTo5(value * -1) * -1;
        }
    }
}
