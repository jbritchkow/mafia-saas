import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class DatabaseHelper {
    private Connection conn;
    private String connectionString;

    public DatabaseHelper(String connString) {
        connectionString = connString;
    }

    public void connect() {
        try {
            conn = DriverManager.getConnection(connectionString);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    public void disconnect() {
        try {
            conn.close();
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
    public boolean assignPlayerRoles(int gameId) {
        return false;
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
            rs = stmt.executeQuery("select * from Players where GameId='" + gameId + "' and State='LIVING'");

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

}
