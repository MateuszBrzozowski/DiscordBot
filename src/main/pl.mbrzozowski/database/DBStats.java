package database;

import helpers.Constants;

public class DBStats extends DBConnector {

    public DBStats() {
        super.database = Constants.DB_STATS_DATABASE;
        super.server = Constants.DB_STATS_SERVER;
        super.user = Constants.DB_STATS_USER;
        super.pass = Constants.DB_STATS_PASS;
        setUrlAndConnect();
    }
}
