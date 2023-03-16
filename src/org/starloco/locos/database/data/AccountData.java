package org.starloco.locos.database.data;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.AbstractDAO;
import org.starloco.locos.object.Account;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountData extends AbstractDAO<Account> {

    public AccountData(HikariDataSource source) {
        super(source);
        logger = (Logger) LoggerFactory.getLogger("factory.Account");
        logger.setLevel(Level.OFF);
    }

    @Override
    public Account load(Object id) {
        Account account = null;
        try {
            String query = "SELECT * FROM accounts WHERE guid = " + id;
            Result result = super.getData(query);
            account = loadFromResultSet(result.resultSet);
            close(result);
            if (account != null) {
                query = "UPDATE accounts SET reload_needed = 0 WHERE guid = "
                        + id;
                super.execute(query);
            }
            logger.debug("Account with id {} successfully loaded", id);
        } catch (Exception e) {
            logger.error("Can't load account with guid" + id, e);
        }
        return account;
    }

    public Account load(String name) {
        Account account = null;
        try {
            String query = "SELECT * FROM accounts WHERE account = '" + name + "';";
            Result result = super.getData(query);
            account = loadFromResultSet(result.resultSet);
            close(result);
            if (account != null) {
                query = "UPDATE accounts SET reload_needed = 0 WHERE guid = '"
                        + account.getUUID() + "';";
                super.execute(query);
                logger.debug("Account with name {} successfully loaded", name);
            } else {
                logger.debug("Account with name {} failed to load", name);
            }
        } catch (Exception e) {
            logger.error("Can't load account with name " + name, e);
        }
        return account;
    }
    public Account loadBySwitchPacketKey(String switchPacketKey) {
        Account account = null;
        try {
            String query = "SELECT * FROM accounts WHERE switchPacketKey LIKE '" + switchPacketKey + "%';";
            Result result = super.getData(query);
            account = loadFromResultSet(result.resultSet);
            close(result);
            if (account != null) {
                query = "UPDATE accounts SET reload_needed = 0 WHERE guid = '"
                        + account.getUUID() + "';";
                super.execute(query);
                logger.debug("Account with switchPacketKey {} successfully loaded", switchPacketKey);
            } else {
                logger.debug("Account with switchPacketKey {} failed to load", switchPacketKey);
            }
        } catch (Exception e) {
            logger.error("Can't load account with switchPacketKey " + switchPacketKey, e);
        }
        return account;
    }
    
    public byte getRecentState(String name)
    {
    	byte state = 0;
        try {
            String query = "SELECT * FROM accounts WHERE account = '" + name + "';";
            Result result = super.getData(query);
            ResultSet resultSet = result.resultSet;

            if (resultSet.next()) 
            	state = resultSet.getByte("logged");
            close(result);
        } catch (Exception e) {
            logger.error("Can't know the state of {}", name);
        }
        return state;
    }
    @Override
    public boolean update(Account account) {
        try {
            String baseQuery = "UPDATE accounts SET "
            		+ "banned = '" + (account.isBanned() ? "1" : "0") + "',"
            		+ "bannedTime = '" + account.getBannedTime() + "',"
            		+ "logged = '" + account.getState()+ "', "
                    + "switchPacketKey = '" + account.getSwitchPacketKey() + "' "
            		+ "WHERE guid = '" + account.getUUID() + "';";

            PreparedStatement statement = getPreparedStatement(baseQuery);
            execute(statement);

            return true;
        } catch (Exception e) {
            logger.error("SQL ERROR, trying rollback", e);
        }
        return false;
    }

    public String exist(String nickname) {
        String name = null;
        try {
            String query = "SELECT * FROM accounts WHERE pseudo = '" + nickname
                    + "';";
            Result result = super.getData(query);
            if (result.resultSet.next())
                name = result.resultSet.getString("account");
            close(result);
            logger.debug("Account with pseudo {} exist", nickname);
        } catch (Exception e) {
            logger.error("Can't load account with pseudo like {}" + nickname, e);
        }
        return name;
    }

    public void resetLogged(int guid, int server) {
        try {
            String baseQuery = "UPDATE accounts SET  logged = '0' WHERE guid = '"
                    + guid + "';";
            PreparedStatement statement = getPreparedStatement(baseQuery);
            execute(statement);

            baseQuery = "UPDATE players SET logged = '0' WHERE server = '"
                    + server + "' AND account = '" + guid + "';";
            statement = getPreparedStatement(baseQuery);
            execute(statement);

        } catch (Exception e) {
            logger.error("SQL ERROR, trying rollback", e);
        }
    }

    public boolean isBannedIp(String ip) {
        boolean banned = false;
        try {
            String query = "SELECT * FROM banip WHERE 'ip' LIKE '" + ip + "';";
            Result result = super.getData(query);
            ResultSet resultSet = result.resultSet;

            if (resultSet.next())
                banned = true;

            close(result);
        } catch (Exception e) {
            logger.error("Can't know if ip {} is banned", ip);
        }
        return banned;
    }
    
    public void refreshBanned(Account account) {
        try {
            String query = "SELECT * FROM accounts WHERE guid = '" + account.getUUID() + "' AND banned = '1';";
            Result result = super.getData(query);
            ResultSet resultSet = result.resultSet;

            if (resultSet.next()) {
            	account.setBanned(true);
            	account.setBannedTime(resultSet.getLong("bannedTime"));
            }

            close(result);
        } catch (Exception e) {
            logger.error("Can't know if name {} is banned", account.getName());
        }
    }

    Account loadFromResultSet(ResultSet resultSet)
            throws SQLException {
        if (resultSet.next())
            return new Account(resultSet.getInt("guid"), resultSet.getString("account").toLowerCase(), resultSet.getString("pass"), resultSet.getString("pseudo"), resultSet.getString("question"), resultSet.getByte("logged"), resultSet.getLong("subscribe"), resultSet.getByte("banned"), resultSet.getLong("bannedTime"), resultSet.getString("switchPacketKey"));
        return null;
    }
}
