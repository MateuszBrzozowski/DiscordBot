package database;

public class DBRanger extends DBConnector {

    public DBRanger() {
        super.database = "rangerbot";
        super.server = "localhost";
        super.user = "root";
        super.pass = "";
        setUrl();
        connect();
    }

}