package ma.youcode.lineperm.dao;

import java.sql.Connection;
import ma.youcode.lineperm.database.DatabaseConnection;

public abstract class AbstractDao<T> implements Dao<T> {

    // protected DatabaseConnection databaseConnection;
    private Connection connection;

    protected AbstractDao() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    protected Connection getConnection() {
        return connection;
    }

}
