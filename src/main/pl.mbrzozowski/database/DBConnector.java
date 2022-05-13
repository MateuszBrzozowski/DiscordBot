package database;

import helpers.RangerLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class DBConnector {
    protected String database = "";
    protected String server = "";
    protected String url = "jdbc:mysql://" + server + "/" + database + "?useUnicode=true&characterEncoding=UTF-8";
    protected String user = "root";
    protected String pass = "";
    private static RangerLogger rangerLogger = new RangerLogger();
    protected static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private static Connection connection = null;

    protected Connection connect() {
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
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(selectQuery);
        } catch (SQLException throwables) {
            rangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", selectQuery);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void executeQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException throwables) {
            rangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", query);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    protected void setUrlAndConnect() {
        this.url = "jdbc:mysql://" + server + "/" + database + "?useUnicode=true&characterEncoding=UTF-8";
        connect();
    }
}
