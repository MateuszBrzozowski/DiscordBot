package database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class DBConnector {
    private static String url ="jdbc:mysql://localhost/rangerbot";
    private static String user ="root";
    private static String pass = "";
    protected static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private static Connection connection = null;

    public static Connection connect() {
        try {
            connection = DriverManager.getConnection(url,user,pass);
            logger.info("Połączono z bazą danych.");
        } catch (SQLException throwables) {
            logger.error("Połączenie z bazą danych nieudane.");
            throwables.printStackTrace();
        }
        return connection;
    }

    public ResultSet executeSelect(String selectQuery){
        connection=connect();
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(selectQuery);
        } catch (SQLException throwables) {
            logger.error("Zapytanie do bazy danych zakończone niepowodzeniem: {}",selectQuery);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void executeQuery(String query){
        connection=connect();
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);

        } catch (SQLException throwables) {
            logger.error("Zapytanie do bazy danych zakończone niepowodzeniem: {}",query);
            throw new RuntimeException(throwables.getMessage());
        }

    }
}
