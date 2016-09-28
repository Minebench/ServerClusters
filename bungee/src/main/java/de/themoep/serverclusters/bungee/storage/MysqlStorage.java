package de.themoep.serverclusters.bungee.storage;

import de.themoep.serverclusters.bungee.ServerClusters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public class MysqlStorage extends ValueStorage {

    private Connection conn;

    private final String dbuser;
    private final String dbpassword;
    private final String dbname;
    private final String dbhost;
    private final int dbport;
    private final String dburl;
    private final String dbtableprefix;

    public MysqlStorage(ServerClusters plugin, String name) throws InvalidPropertiesFormatException, SQLException {
        super(plugin, name);

        dbuser = plugin.getConfig().getString("mysql.user");
        dbpassword = plugin.getConfig().getString("mysql.password");
        dbname = plugin.getConfig().getString("mysql.dbname");
        dbhost = plugin.getConfig().getString("mysql.host");
        dbport = plugin.getConfig().getInt("mysql.port");
        dbtableprefix = plugin.getConfig().getString("mysql.tableprefix", "serverclusters_");

        if (dbhost != null && dbuser != null && dbpassword != null && dbname != null && dbport > 0) {
            dburl = ("jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname);

            plugin.getLogger().info("Checking Database Connection...");
            checkConnection();

            plugin.getLogger().info("Initializing Database...");
            initDb();
        } else {
            plugin.getLogger().warning("MySQL settings not or not fully configured! Falling back to YAML backend!");
            throw new InvalidPropertiesFormatException("We are missing at least one parameter to establish a database connection!");
        }
    }

    /**
     * Connects to the Database.
     */
    private void connectDb() throws SQLException {
        plugin.getLogger().info("Connecting to Database...");
        conn = (Connection) DriverManager.getConnection(dburl, dbuser, dbpassword);
        conn.setAutoCommit(true);
    }

    /**
     * Initializes the databases for the plugin if they don't exist.
     */
    private void initDb() {
        try {
            Statement sta = (Statement) conn.createStatement();
            sta.execute("CREATE TABLE IF NOT EXISTS `" + dbtableprefix + "_" + name + "` ( `playerid` varchar(52) NOT NULL, `value` count(16) NOT NULL, PRIMARY KEY (`playerid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            sta.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not initialize the tables! Error: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * Checks if the connection to the database still exists and reconnects if it doesn't.
     */
    private void checkConnection() throws SQLException {
        if (conn == null || !conn.isValid(1))
            connectDb();
    }

    @Override
    public String getValue(UUID playerId) {
        try {
            PreparedStatement sta;
            sta = conn.prepareStatement("SELECT `value`from " + dbtableprefix + "_" + name + " WHERE playerid=?");
            sta.setString(1, playerId.toString());
            ResultSet rs = sta.executeQuery();
            sta.close();
            if (rs.next()) {
                return rs.getString("value");
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL-Error! Something went wrong while fetching data for player with the id " + playerId + "! Does the table \"" + dbtableprefix + "_" + name + "\" exist?");
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error while closing the database connection for " + name);
            e.printStackTrace();
        }
    }
}
