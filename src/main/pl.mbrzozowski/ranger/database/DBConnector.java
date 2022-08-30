package ranger.database;

import ranger.helpers.RangerLogger;

import java.sql.*;

public abstract class DBConnector {
    protected String database = "";
    protected String server = "";
    protected String url = "jdbc:mysql://" + server + "/" + database + "?useUnicode=true&characterEncoding=UTF-8";
    protected String user = "root";
    protected String pass = "";
    private static Connection connection = null;

    protected void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLTimeoutException e) {
            RangerLogger.info("Przkroczony czass łączenia z bazą danych");
        } catch (SQLException throwables) {
            RangerLogger.info("Połączenie z bazą danych nieudane.");
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            RangerLogger.info("JDBC driver not load and register.");
        }
    }

    public ResultSet executeSelect(String selectQuery) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(selectQuery);
        } catch (SQLException throwables) {
            RangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", selectQuery);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    public void executeQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException throwables) {
            RangerLogger.info("Zapytanie do bazy danych zakończone niepowodzeniem: {}", query);
            throw new RuntimeException(throwables.getMessage());
        }
    }

    protected void setUrlAndConnect() {
        this.url = "jdbc:mysql://" + server + "/" + database + "?useUnicode=true&characterEncoding=UTF-8";
        connect();
    }
}
