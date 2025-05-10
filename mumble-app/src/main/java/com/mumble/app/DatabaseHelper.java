package com.mumble.app;

import java.sql.*;

public class DatabaseHelper {
    
    // method to establish the connection
    public static Connection connect() throws SQLException{
        return DatabaseManager.getConnection();
    }

    // method to create the database schema
    public static void createSchema(){
        String createUserTable = "CREATE TABLE IF NOT EXISTS users( " +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "username TEXT NOT NULL UNIQUE, " +
                                    "email TEXT NOT NULL UNIQUE, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                    "phone TEXT," +
                                    "password TEXT NOT NULL);";

        String createMessageTable = "CREATE TABLE IF NOT EXISTS messages(" + 
                                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                                    "message TEXT NOT NULL, " +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " + 
                                    "FOREIGN KEY (user_id) REFERENCES users(id))";

        try(Connection conn = connect(); Statement stmt = conn.createStatement()){
            stmt.execute(createUserTable);
            stmt.execute(createMessageTable);
            System.out.println("Users and Messages tables created!");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    // method to save a user to the database
    public static void saveUser(String username, String password, String email, String phone, String timestamp){
        String sql = "INSERT INTO users(username, password, email, phone, timestamp) VALUES(?, ?, ?, ?, ?)";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, timestamp);
            pstmt.executeUpdate();
            System.out.println("User saved!");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void saveMessage(int id, String message){
        String sql = "INSERT INTO messages(user_id, message) VALUES(?, ?)";

        try(Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
            System.out.println("Message saved!");
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    // method to get the chat history
    public static void getChatHistory(){
        String sql = "SELECT u.username, m.message, m.timestamp FROM messages WITH m" +
                    "JOIN users u ON m.user_id = u.id ORDER BY m.timestamp";

        try(Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql) ){
            while(rs.next()){
                String username = rs.getString("username");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                System.out.println(username + "(" + timestamp + "): " + message);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}
