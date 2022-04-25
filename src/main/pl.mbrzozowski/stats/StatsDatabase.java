package stats;

import database.DBConnector;
import database.DBFactory;
import database.DBType;
import database.Factory;

public class StatsDatabase {

    private Factory factory = new DBFactory();
    private DBConnector connector = factory.createDB(DBType.STATS);

}
