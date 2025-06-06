package com.mumble.app.Panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.*;

import com.mumble.app.MumbleApp;
import com.mumble.app.User;
import com.mumble.app.ClientServerConnection.ChatClientConnection;
import com.mumble.app.ClientServerConnection.ClientHandler;
import com.mumble.app.UIComponents.NavBar.NavBar;

public class FeedPanel extends JPanel{

    private ChatClientConnection clientConn;
    private List<User> connectedUsers;
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private User user;
    private MumbleApp app;

    public FeedPanel(MumbleApp a, ChatClientConnection conn, User u, List<User> users, JPanel view, JScrollPane sp){
        this.app = a;
        this.clientConn = conn;
        this.user = u;
        this.viewPanel = view;
        this.scrollPane = sp;

        // set the layout
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // set the insets for all components
        gbc.insets = new Insets(2,2,2,2);

        // column 0 and row 0
        gbc.gridx = 0;
        gbc.gridy = 0;

        // make a navbar panel
        NavBar navBar = new NavBar(this.app, this.clientConn, this.user, this.connectedUsers, this.viewPanel, this.scrollPane);


    }
    
}
