package ma.youcode.lineperm.dao;

import java.sql.Connection;
import ma.youcode.lineperm.database.DatabaseConnection;

public abstract class AbstractDao<T> implements Dao<T> {

    protected DatabaseConnection databaseConnection;

    protected DatabaseConnection getInstance() {

        this.databaseConnection = DatabaseConnection.getInstance();
        return this.databaseConnection;
    }

    protected Connection getConnection() {
        return getInstance().getConnection();
    }

}
