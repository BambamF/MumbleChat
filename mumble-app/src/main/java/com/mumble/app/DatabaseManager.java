package com.mumble.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A DatabaseManager provides methods to query user data from the database
 */
public class DatabaseManager{

    private static final String DB_URL = "jdbc:sqlite:mumble.db";

    /**
     * Connects to the database using a DriverManager
     * @return
     * @throws SQLException
     */
    public  static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Retrieves all messages in the database
     * @return
     */
    public static List<Message> getAllMessages(){
        String sql = "SELECT * FROM messages ORDER BY timestamp DESC";
        List<Message> messages = new ArrayList<>();

        try(Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                int id = rs.getInt("user_id");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");

                messages.add(new Message(id, "username", 0, message, timestamp));
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return messages;
    }

    /**
     * Returns whether the email is in the database as a boolean
     * @param email the email to be queried as a String
     * @return whether the email is in the database as a boolean
     */
    public static boolean doesEmailExist(String email){
        String sql = "SELECT 1 from users WHERE email = ?";

        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return true;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the user ID of the given username and password hash as an int
     * @param username the username to be queried as a String
     * @param password the password hash to be queried as a String
     * @return the user ID as an int
     */
    public static int getUserId(String username, String password){
        String sql = "SELECT id FROM users WHERE username = ?";

        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt("id");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Returns the password hash associated with the given username as a String
     * @param username the username to be queried as a String
     * @return the password hash as a String
     */
    public static String getPassword(String username){
        String sql = "SELECT password from users WHERE username = ?";

        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getString("password");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns whether the username exists in the database as a boolean
     * @param username the username to be queried as a String
     * @return whether the username exists in the database as a boolean
     */
    public static boolean usernameExists(String username){
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try(Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }
    
}
