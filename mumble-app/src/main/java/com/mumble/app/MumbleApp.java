package com.mumble.app;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * A MumbleApp launches the chat GUI
 */
public class MumbleApp extends JFrame {
    
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    public static final int COLUMN_WIDTH = 20;
    private int userId;
    private static ChatClientConnection clientConn;

    /**
     * Instantiates the chat GUI
     */
    public MumbleApp(){
        super("MumbleApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);

        // creates the database user and message schema
        DatabaseHelper.createSchema();
        // DatabaseHelper.resetMessagesTable();                 // THIS IS ONLY FOR TESTING PURPOSES!!!!!!

        // card layout to switch pages
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // create pages
        LoginPanel loginPanel = new LoginPanel(this, clientConn);
        ChatPanel chatPanel = new ChatPanel(this, clientConn);
        CreateAccountPanel createAccountPanel = new CreateAccountPanel(this);

        // add the pages to the mainPanel
        mainPanel.add(loginPanel, "login");
        mainPanel.add(chatPanel, "chat");
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
    public static void showChatPage(){
        cardLayout.show(mainPanel, "chat");
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

    /**
     * Activates the connection to the server via a ChatClientConnection
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    public static void connectToServer(JPanel viewPanel, JScrollPane scrollPane){
        try{
            clientConn = new ChatClientConnection("localhost", ChatServer.PORT, viewPanel, scrollPane);
            System.out.println("Chat client connection established");
        }
        catch(IOException e){
            System.err.println("Chat client connection failed");
            showMessage( "Connection to server unsuccessful, retrying....", viewPanel, scrollPane);

            // retry if the connection failed
            reconnectToServer(viewPanel, scrollPane);
            e.printStackTrace();
        }
    }

    /**
     * Reconnect to the server after a failed connection attempt
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    public static void reconnectToServer(JPanel viewPanel, JScrollPane scrollPane){
        Timer timer = new Timer(3000, e -> connectToServer(viewPanel, scrollPane) );
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
