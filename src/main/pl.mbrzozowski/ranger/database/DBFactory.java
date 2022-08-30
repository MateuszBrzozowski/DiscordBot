package ranger.database;

public class DBFactory extends Factory {

    @Override
    public DBConnector createDB(DBType type) {
        switch (type) {
            case RANGER:
                return new DBRanger();
            case STATS:
                return new DBStats();
            default:
                throw new UnsupportedOperationException("No such type");
        }
    }
}
