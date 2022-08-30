package ranger.database;

public abstract class Factory {
    abstract public DBConnector createDB(DBType type);
}
