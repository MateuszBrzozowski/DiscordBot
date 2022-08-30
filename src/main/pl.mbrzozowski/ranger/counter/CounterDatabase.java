package ranger.counter;

import ranger.database.DBConnector;
import ranger.database.DBFactory;
import ranger.database.DBType;
import ranger.database.Factory;
import ranger.helpers.RangerLogger;

import java.sql.ResultSet;

class CounterDatabase {

    private static final String COUNTER = "counter";
    private final Factory factory = new DBFactory();
    private final DBConnector connector = factory.createDB(DBType.RANGER);

    ResultSet pullAllUsers() {
        ResultSet resultSet = null;
        String query = "SELECT * FROM `" + COUNTER + "`";
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            RangerLogger.info("Tworzę tabelę counter");
            createTableCounter();
        }
        return resultSet;
    }

    private void createTableCounter() {
        String queryCreate = "CREATE TABLE " + COUNTER + "(" +
                "userID VARCHAR(30) PRIMARY KEY," +
                "msgAll INT(9))";
        connector.executeQuery(queryCreate);
    }

    public void addNewUser(String userID) {
        String query = "INSERT INTO " + COUNTER + " (`userID`,`msgAll`) " +
                "VALUES (\"" + userID + "\",0)";
        connector.executeQuery(query);
    }

    public void updateUser(String userID, int msgAll) {
        String query = "UPDATE " + COUNTER + " " +
                "SET msgAll=" + msgAll + " " +
                "WHERE userID=\"" + userID + "\"";
        connector.executeQuery(query);
    }
}
