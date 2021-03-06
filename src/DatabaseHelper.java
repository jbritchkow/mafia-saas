import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import javax.sql.PooledConnection;
import java.sql.*;
import java.util.HashMap;

public class DatabaseHelper {
    private PooledConnection poolConn;
    private Connection conn;
    private String connectionString;
    private MysqlConnectionPoolDataSource ds;
    public enum States {
        LIVING ("LIVING"),
        DEAD ("DEAD"),
        HEALED ("HEALED"),
        MARKED ("MARKED");
        private final String name;
        private States (String s) {
            name = s;
        }
        public boolean equalsState(String otherName) {
            return name.equals(otherName);
        }
        public String toString() {
            return this.name;
        }
    };

    public DatabaseHelper(String connString) {
        connectionString = connString;
        ds = new MysqlConnectionPoolDataSource();
        ds.setUrl(connString);
    }

    public void connect() {
        try {
            //conn = DriverManager.getConnection(connectionString);
            poolConn = ds.getPooledConnection();
            conn = poolConn.getConnection();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    public void disconnect() {
        try {
            conn.close();
            conn = null;
            poolConn.close();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        conn = null;
    }

    public boolean checkNameAndAddPlayerToGame(int gameId, int playerId, String playerName) {
        boolean uniqueName = true;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" + gameId + "' and Name='" + playerName + "'");
            //Check if this name already exists
            while (rs.next()) {
                uniqueName = false;
                break;
            }

            //Name doesn't exist yet
            if (uniqueName) {
                rs.close();
                rs = null;
                stmt.close();
                stmt = conn.createStatement();
                stmt.execute("insert into Players (Id, Name, GameId, State) values ('" +
                        playerId + "','" + playerName + "','" + gameId + "','LIVING')");
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (Exception e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) {}
            disconnect();
        }

        return uniqueName;
    }
    public boolean assignRoleToPlayer(int gameId, int playerId, String role) {
        Statement stmt = null;
        boolean success = true;
        try {
            connect();
            stmt = conn.createStatement();
            stmt.execute("update Players set Role='" + role +
                    "' where GameId='" + gameId + "' and Id='" + playerId + "'");
        }
        catch (SQLException ex) {
            success = false;
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return success;
    }
    public boolean assignStateToPlayer(int gameId, int playerId, String state) {
        Statement stmt = null;
        boolean success = true;
        try {
            connect();
            stmt = conn.createStatement();
            if (States.MARKED.equalsState(state))
                stmt.execute("update Players set State='" + state +
                        "' where GameId='" + gameId + "' and Id='" + playerId + "' and State!='" + States.HEALED + "'");
            else
                stmt.execute("update Players set State='" + state +
                    "' where GameId='" + gameId + "' and Id='" + playerId + "'");
        }
        catch (SQLException ex) {
            success = false;
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return success;
    }
    public boolean resetPlayerStatesForNextTurn(int gameId) {
        Statement stmt = null;
        boolean success = true;
        try {
            connect();
            stmt = conn.createStatement();
            stmt.execute("update Players set State='" + States.DEAD +
                    "' where GameId='" + gameId + "' and State='" + States.MARKED + "'");

            stmt = conn.createStatement();
            stmt.execute("update Players set State='" + States.LIVING +
                    "' where GameId='" + gameId + "' and State='" + States.HEALED + "'");
        }
        catch (SQLException ex) {
            success = false;
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return success;
    }
    public String getPlayerRole(int gameId, int playerId) {
        Statement stmt = null;
        ResultSet rs = null;
        String role = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" + gameId + "' and Id='" + playerId+ "'");
            if (!rs.next())
                throw new Exception("Could not find player");
            role = rs.getString("Role");
        }
        catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        catch (Exception ex) {System.out.println(ex.getMessage()); }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return role;
    }
    public String getPlayerStatus(int gameId, int playerId) {
        Statement stmt = null;
        ResultSet rs = null;
        String role = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" + gameId + "' and Id='" + playerId+ "'");
            if (!rs.next())
                throw new Exception("Could not find player");
            role = rs.getString("Status");
        }
        catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        catch (Exception ex) {System.out.println(ex.getMessage()); }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return role;
    }
    public HashMap<Integer, String> getAllPlayers(int gameId) {
        HashMap<Integer, String> players = new HashMap<Integer, String>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" + gameId + "'");

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                players.put(id, name);
            }
        }
        catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return players;
    }
    public HashMap<Integer, String> getLivingPlayers(int gameId) {
        HashMap<Integer, String> players = new HashMap<Integer, String>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" +
                    gameId + "' and (State='" + States.LIVING + "' or State='" + States.HEALED +
                    "' or State='" + States.MARKED + "')");

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                players.put(id, name);
            }
        }
        catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return players;
    }
    public HashMap<Integer, String> getPlayersWithStatus(int gameId, String state) {
        HashMap<Integer, String> players = new HashMap<Integer, String>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from Players where GameId='" +
                    gameId + "' and State='" + state + "'");

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                players.put(id, name);
            }
        }
        catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return players;
    }
    public boolean deleteGame(int gameId) {
        Statement stmt = null;
        boolean success = true;
        try {
            connect();
            stmt = conn.createStatement();
            stmt.execute("delete from Players where GameId='" + gameId + "'");
        }
        catch (SQLException ex) {
            success = false;
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {}
            disconnect();
        }
        return success;
    }

}
