package database;

import helpers.Constants;

public class DBRanger extends DBConnector {

    public DBRanger() {
        super.database = Constants.DB_RANGER_DATABASE;
        super.server = Constants.DB_RANGER_SERVER;
        super.user = Constants.DB_RANGER_USER;
        super.pass = Constants.DB_RANGER_PASS;
        setUrlAndConnect();
    }

}