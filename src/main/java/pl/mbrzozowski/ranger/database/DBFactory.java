package pl.mbrzozowski.ranger.database;

public class DBFactory extends Factory {

    @Override
    public DBConnector createDB(DBType type) {
        return switch (type) {
            case RANGER -> new DBRanger();
            case STATS -> new DBStats();
            default -> throw new UnsupportedOperationException("No such type");
        };
    }
}
