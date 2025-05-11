package com.mumble.app;

import javax.swing.*;
import java.awt.*;

/**
 * A MumbleApp launches the chat GUI
 */
public class MumbleApp extends JFrame {
    
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    public static final int COLUMN_WIDTH = 20;
    private int userId;

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
        LoginPanel loginPanel = new LoginPanel(this);
        ChatPanel chatPanel = new ChatPanel(this);
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
