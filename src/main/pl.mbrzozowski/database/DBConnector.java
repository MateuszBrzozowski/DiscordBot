package database;

import helpers.RangerLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DBConnector {
    private static String database = "rangerbot";
    private static String serwer = "localhost";
    private static String url = "jdbc:mysql://" + serwer + "/" + database + "?useUnicode=true&characterEncoding=UTF-8";
    private static String user = "root";
    private static String pass = "";
    private static RangerLogger rangerLogger = new RangerLogger();
    protected static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private static Connection connection = null;

    public DBConnector() {
        connection = connect();
    }

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, pass);
            logger.info("Połączono z bazą danych.");
        } catch (SQLTimeoutException e) {
            rangerLogger.info("Przkroczony czass łączenia z bazą danych");
        } catch (SQLException throwables) {
            rangerLogger.info("Połączenie z bazą danych nieudane.");
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            rangerLogger.info("JDBC driver not load and register.");
        }
        return connection;
    }

    public ResultSet executeSelect(String selectQuery) {
//        connection = connect();
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(selectQuery);
        } catch (SQLException throwables) {
            rangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", selectQuery);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void executeQuery(String query) {
//        connection = connect();
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException throwables) {
            rangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", query);
            throw new RuntimeException(throwables.getMessage());
        }

    }
}
