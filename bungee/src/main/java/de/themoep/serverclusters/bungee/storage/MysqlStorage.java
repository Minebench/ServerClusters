package de.themoep.serverclusters.bungee.storage;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.serverclusters.bungee.ServerClusters;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;
import java.util.logging.Level;

public class MysqlStorage extends ValueStorage {

    private final HikariDataSource ds;

    private final String dbtableprefix;

    public MysqlStorage(ServerClusters plugin, String name) throws InvalidPropertiesFormatException, SQLException {
        super(plugin, name);

        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.dbname");
        dbtableprefix = plugin.getConfig().getString("mysql.tableprefix", "serverclusters_");

        if (host != null && database != null && port > 0) {

            ds = new HikariDataSource();
            ds.setDataSource(new MariaDbDataSource(host, port, database));
            ds.setUsername(plugin.getConfig().getString("mysql.user"));
            ds.setPassword(plugin.getConfig().getString("mysql.password"));
            ds.setConnectionTimeout(5000);

            plugin.getLogger().info("Initializing Database...");
            initDb();
        } else {
            plugin.getLogger().warning("MySQL settings not or not fully configured! Falling back to YAML backend!");
            throw new InvalidPropertiesFormatException("We are missing at least one parameter to establish a database connection!");
        }
    }

    /**
     * Initializes the databases for the plugin if they don't exist.
     */
    private void initDb() {
        try (Connection conn = ds.getConnection();
             Statement sta = conn.createStatement()){
            sta.execute("CREATE TABLE IF NOT EXISTS `" + dbtableprefix + name + "` ( `playerid` char(36) NOT NULL, `value` varchar(256) NOT NULL, PRIMARY KEY (`playerid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            sta.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize the tables! ", e);
        }
    }

    @Override
    public String getValue(UUID playerId) {
        String sql = "SELECT `value`from " + dbtableprefix + name + " WHERE playerid=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, playerId.toString());
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL-Error! Something went wrong while fetching data for player with the id " + playerId + "! Does the table \"" + dbtableprefix + "_" + name + "\" exist?", e);
            return null;
        }
    }

    @Override
    public void putValue(final UUID playerId, final String value) {
        if (value.length() > 256) {
            throw new IllegalArgumentException("Value is longer than 256 chars! (" + value.length() + ")");
        }
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            String sql = "INSERT INTO " + dbtableprefix + name + " (`playerid`,`value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE value=VALUES(`value`)";
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement(sql)) {
                sta.setString(1, playerId.toString());
                sta.setString(2, value);
                sta.executeQuery();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL-Error! Something went wrong while inserting data for player with the id " + playerId + "!", e);
            }
        });
    }

    @Override
    public void close() {
        ds.close();
    }
}
