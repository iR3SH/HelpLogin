package org.starloco.locos.database.data;

import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.AbstractDAO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Locos on 15/09/2015.
 */
public class WorldEntityData extends AbstractDAO<Object> {

    private int nextPlayerId;

    public WorldEntityData(HikariDataSource dataSource) {
        super(dataSource);
        this.load(null);
    }

    @Override
    public Object load(Object obj) {
        Result result = null;
        try {
            result = getData("SELECT MAX(id) AS max FROM `players`;");
            ResultSet RS = result.resultSet;
            boolean found = RS.first();
            if (found) this.nextPlayerId = RS.getInt("max");
            else this.nextPlayerId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData load", e);
        } finally {
            close(result);
        }

        return null;
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }
    
    public synchronized int getNextPlayerId() {
        return ++nextPlayerId;
    }
}
