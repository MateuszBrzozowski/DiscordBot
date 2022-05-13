package counter;

import database.DBConnector;
import database.DBFactory;
import database.DBType;
import database.Factory;
import helpers.RangerLogger;

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
