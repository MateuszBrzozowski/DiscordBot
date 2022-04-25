package database;

public class DBStats extends DBConnector {

    public DBStats() {
        super.database = "rangerbot";
        super.server = "localhost";
        super.user = "root";
        super.pass = "";
        setUrl();
        connect();
    }
}
