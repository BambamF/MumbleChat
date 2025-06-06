package com.mumble.app;

import javax.swing.*;

import com.mumble.app.ClientServerConnection.ChatClientConnection;
import com.mumble.app.ClientServerConnection.ChatServer;
import com.mumble.app.DB.DatabaseHelper;
import com.mumble.app.Panels.ChatPanel;
import com.mumble.app.Panels.CreateAccountPanel;
import com.mumble.app.Panels.LoginPanel;

import java.awt.*;
import java.io.IOException;

/**
 * A MumbleApp launches the chat GUI
 */
public class MumbleApp extends JFrame {
    
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    public static final int COLUMN_WIDTH = 20;
    private ChatClientConnection clientConn;
    private User user;
    private ChatPanel chatPanel;
    private LoginPanel loginPanel;
    private String relativePath = System.getProperty("user.dir");

    /**
     * Instantiates the chat GUI
     */
    public MumbleApp(){
        super("MumbleApp");

        // gracefully closes the connection and shuts down the app on exit
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Use overridden window closer instead
        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                shutDownApp();
            }
        });
        setSize(1000, 750);

        // creates the database user and message schema
         DatabaseHelper.createSchema();
        // DatabaseHelper.resetMessagesTable();                 // THIS IS ONLY FOR TESTING PURPOSES!!!!!!
        // DatabaseHelper.resetUsersTable();                 // THIS IS ONLY FOR TESTING PURPOSES!!!!!!
        // DatabaseHelper.resetBothTables();                 // THIS IS ONLY FOR TESTING PURPOSES!!!!!!

        // card layout to switch pages
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // create pages
        LoginPanel loginPanel = new LoginPanel(this);
        this.loginPanel = loginPanel;
        CreateAccountPanel createAccountPanel = new CreateAccountPanel(this);

        // add the pages to the mainPanel
        mainPanel.add(loginPanel, "login");
        mainPanel.add(createAccountPanel, "create-account");

        // add the mainPanel to the frame
        add(mainPanel);

        // show the login page
        showLoginPage();

        // make the app visible
        setVisible(true);
    }

    /**
     * Changes the view to the chat page
     */
    public void showChatPage() {
        cardLayout.show(mainPanel, "chat");
    }

    /**
     * Returns the relative path
     * @return the relative path as a String
     */
    public String getReletivePath(){
        return this.relativePath;
    }

    /**
     * Closes the connection and shutsdown the app
     */
    public void shutDownApp(){
        if(this.clientConn != null){
            this.clientConn.closeConnection();
        }
        dispose();
        System.exit(0);
    }

    /**
     * Changes the view to the login page
     */    
    public static void showLoginPage(){
        cardLayout.show(mainPanel, "login");
    }

    /**
     * Changes the view to the create account page
     */    
    public static void showCreateAccountPage(){
        cardLayout.show(mainPanel, "create-account");
    }

    public LoginPanel getLoginPanel(){
        return this.loginPanel;
    }

    public ChatPanel getChatPanel(){
        return this.chatPanel;
    }

    /**
     * Activates the connection to the server via a ChatClientConnection
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    public void connectToServer(JPanel viewPanel, JScrollPane scrollPane, User user){
        try{
            clientConn = new ChatClientConnection("localhost", ChatServer.PORT, viewPanel, scrollPane, user, this);
            System.out.println("Chat client connection established in App");
        }
        catch(IOException e){
            System.err.println("Chat client connection failed");
            showMessage( "Connection to server unsuccessful, retrying....", viewPanel, scrollPane);

            // retry if the connection failed
            reconnectToServer(viewPanel, scrollPane, user);
            e.printStackTrace();
        }
    }

    /**
     * Reconnect to the server after a failed connection attempt
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    public void reconnectToServer(JPanel viewPanel, JScrollPane scrollPane, User user){
        Timer timer = new Timer(3000, e -> connectToServer(viewPanel, scrollPane, user) );
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Adds a label to the chat panel to notify the user
     * @param message the notification message as a String
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    public static void showMessage(String message, JPanel viewPanel, JScrollPane scrollPane){

        // wrap the message label in a new panel to control layout
        JPanel wrapper = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel(message);

        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        messageLabel.setForeground(Color.GRAY);

        wrapper.add(messageLabel, BorderLayout.EAST);

        viewPanel.add(wrapper);
        viewPanel.revalidate();
        viewPanel.repaint();

        // scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) scrollPane.getComponent(0)).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Sets the user object after sign in
     * @param username the username to be used as a String
     */
    public void setUser(String username, int userId){
        this.user = new User(username, 0, userId);
    }

    /**
     * Returns the user
     * @return the User object
     */
    public User getUser(){
        return this.user;
    }

    /**
     * Returns the client connection object
     * @return the ChatClientConnection object
     */
    public ChatClientConnection getClientConn(){
        return this.clientConn;
    }

    public void setChatPanel(ChatPanel cp) {
        this.chatPanel = cp;
        mainPanel.add(cp, "chat");
    }


    public static void main(String[] args) {

        // start the server
        Thread serverThread = new Thread(new ChatServer());
        serverThread.start();

        try{
            Thread.sleep(500);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        // start the GUI
        SwingUtilities.invokeLater(MumbleApp::new);
    }
}
