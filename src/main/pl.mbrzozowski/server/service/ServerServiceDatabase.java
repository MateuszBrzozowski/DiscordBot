package server.service;

import database.DBConnector;
import helpers.RangerLogger;

import java.sql.ResultSet;

public class ServerServiceDatabase {

    private final String SERVER_SERVICE = "serverservice";
    private DBConnector connector = new DBConnector();

    ResultSet pullAllUsers() {
        ResultSet resultSet = null;
        String query = "SELECT * FROM `" + SERVER_SERVICE + "`";
        try {
            resultSet = connector.executeSelect(query);
        } catch (Exception e) {
            RangerLogger.info("Tworzę tabelę " + SERVER_SERVICE);
            createTableServerService();
        }
        return resultSet;
    }

    public void addNewUser(String userID, String channelID, String userName, int typeOfReport) {
        String query = "INSERT INTO " + SERVER_SERVICE + " (`channelID`,`userID`,`userName`,`typeOfReport`) " +
                "VALUES (\"" + channelID + "\",\"" + userID + "\",\"" + userName + "\"," + typeOfReport + ")";
        connector.executeQuery(query);
    }

    public void removeRecord(String channelID) {
        String query = "DELETE FROM `" + SERVER_SERVICE + "` WHERE channelID=\"%s\"";
        connector.executeQuery(String.format(query, channelID));
    }

    private void createTableServerService() {
        String queryCreate = "CREATE TABLE " + SERVER_SERVICE + "(" +
                "channelID VARCHAR(30) PRIMARY KEY," +
                "userID VARCHAR(30) NOT NULL," +
                "userName VARCHAR(30) NOT NULL," +
                "typeOfReport TINYINT(1) NOT NULL)";
        connector.executeQuery(queryCreate);
    }

}
