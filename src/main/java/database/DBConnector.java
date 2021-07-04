package database;

import helpers.RangerLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class DBConnector {
//    private static String url ="jdbc:mysql://localhost/rangerbot";
    private static String database ="heroku_81d940f3eb8f149";
    private static String serwer ="eu-cdbr-west-01.cleardb.com";
    private static String url ="jdbc:mysql://"+serwer+"/"+database;
    private static String user ="b5abcf8a4dffdd";
    private static String pass = "3ec5318c";
    private RangerLogger rangerLogger = new RangerLogger();
    protected static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private static Connection connection = null;

    public static Connection connect() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url,user,pass);
            logger.info("Połączono z bazą danych.");
        } catch (SQLTimeoutException e){
            logger.info("Przekroczony czas łączenia");
        } catch (SQLException throwables) {
            logger.error("Połączenie z bazą danych nieudane.");
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.info("JDBC driver not load and register.");
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
