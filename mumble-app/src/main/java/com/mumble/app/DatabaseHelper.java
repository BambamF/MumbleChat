package com.mumble.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A DatabaseHelper provides methods to create database schema, save users, save messages and retrieve messages
 */
public class DatabaseHelper {
    
    /**
     * Establishes the connection via the DatabaseManager
     * @return the Connection object
     * @throws SQLException if the connection request fails
     */
    public static Connection connect() throws SQLException{
        return DatabaseManager.getConnection();
    }

    /**
     * Creates the database schema for users and messages
     */
    public static void createSchema(){
        String createUserTable = "CREATE TABLE IF NOT EXISTS users( " +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "username TEXT NOT NULL UNIQUE, " +
                                    "email TEXT NOT NULL UNIQUE, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                    "phone TEXT, " +
                                    "public_key TEXT, " +
                                    "password TEXT NOT NULL);";

        String createMessageTable = "CREATE TABLE IF NOT EXISTS messages(" + 
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "user_id INTEGER NOT NULL, " +
                                    "username TEXT NOT NULL, " + 
                                    "message TEXT NOT NULL, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
                                    "FOREIGN KEY (user_id) REFERENCES users(id))";

        // connect to the database and create the tables using sql statements                            
        try(Connection conn = connect(); 
            Statement stmt = conn.createStatement()){

            stmt.execute(createUserTable);
            stmt.execute(createMessageTable);
            System.out.println("Users and Messages tables created!");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Saves the user to the database
     * @param username the username as a String
     * @param password the password as a hashed String
     * @param email the email address as a String
     * @param phone the phone number as a String
     * @param timestamp the timestamp as a String
     */
    public static void saveUser(String username, String password, String email, String phone, String publicKey,String timestamp){
        String sql = "INSERT INTO users(username, password, email, phone, public_key, timestamp) VALUES(?, ?, ?, ?, ?, ?)";

        // connect to the database and save the data safely using prepared statements
        try(Connection conn = connect(); 
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, publicKey);
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();
            System.out.println("User saved!");
            
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Saves messages to the database
     * @param message the message to be saved as a Message object
     * @param id the users ID as an int
     * @param userId the users ID as an int
     * @param timestamp the timestamp of the message as a String
     */
public static void saveMessage(int userId, String username, String message, String timestamp){
    String sql = "INSERT INTO messages(user_id, username, message, timestamp) VALUES(?, ?, ?, ?)";

    try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
        pstmt.setInt(1, userId);
        pstmt.setString(2, username);
        pstmt.setString(3, message);
        pstmt.setString(4, timestamp);
        pstmt.executeUpdate();
        System.out.println("Message saved!");
    }
    catch(SQLException e){
        e.printStackTrace();
    }
}

    /**
     * Drops and recreates the messages table
     */
    public static void resetMessagesTable() {
        String drop = "DROP TABLE IF EXISTS messages;";
        String create = "CREATE TABLE IF NOT EXISTS messages(" + 
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "user_id INTEGER NOT NULL, " +
                                    "username TEXT NOT NULL, " + 
                                    "message TEXT NOT NULL, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
                                    "FOREIGN KEY (user_id) REFERENCES users(id))";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(drop);
            stmt.execute(create);
            System.out.println("Messages table reset successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Drops and recreates the messages table
     */
    public static void resetUsersTable() {
        String drop = "DROP TABLE IF EXISTS users;";
        String create = "CREATE TABLE IF NOT EXISTS users( " +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "username TEXT NOT NULL UNIQUE, " +
                                    "email TEXT NOT NULL UNIQUE, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                    "phone TEXT, " +
                                    "public_key TEXT, " +
                                    "password TEXT NOT NULL);";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(drop);
            stmt.execute(create);
            System.out.println("Users table reset successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void resetBothTables(){
        resetMessagesTable();
        resetUsersTable();
    }

    /**
     * Retrieves the chat history from the database
     */
    public static List<Message> getChatHistory(){
        String sql = "SELECT m.user_id, u.username, m.message, m.timestamp FROM messages m " +
                    "JOIN users u ON m.user_id = u.id ORDER BY m.timestamp";

        List<Message> messages = new ArrayList<>();

        try(Connection conn = connect(); 
            Statement stmt = conn.createStatement(); 
            ResultSet rs = stmt.executeQuery(sql) ){

            while(rs.next()){
                String username = rs.getString("username");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                Message finalMessage = new Message(username, 0, timestamp, message);
                messages.add(finalMessage);
                System.out.println(username + "(" + timestamp + "): " + message);
            }

        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return messages;
    }
}
