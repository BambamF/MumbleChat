package com.mumble.app.UIComponents.NavBar;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.swing.*;

import com.mumble.app.MumbleApp;
import com.mumble.app.User;
import com.mumble.app.ClientServerConnection.ChatClientConnection;
import com.mumble.app.ClientServerConnection.ClientHandler;
import com.mumble.app.Utils.ButtonFactory;

public class NavBar extends JPanel{
    private ChatClientConnection clientConn;
    private List<ClientHandler> connectedUsers;
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private User user;
    private MumbleApp app;
    private String logoPath;
    private String dotIconPath;
    private String dashIconPath;
    private String searchIconPath;
    private String notificationIconPath;
    private String profileIconPath;
    private boolean menuActive = false;

    public NavBar(MumbleApp a, ChatClientConnection conn, User u, List<User> users, JPanel view, JScrollPane sp){
        this.app = a;
        this.clientConn = conn;
        this.user = u;
        this.viewPanel = view;
        this.scrollPane = sp;

        // set the layout
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);

        logoPath = app.getReletivePath() + File.separator + "public" + File.separator + "MumbleLogo.png";
        dotIconPath = app.getReletivePath() + File.separator + "public" + File.separator + "dotIcon.png";
        dashIconPath = app.getReletivePath() + File.separator + "public" + File.separator + "dashIcon.png";
        searchIconPath = app.getReletivePath() + File.separator + "public" + File.separator + "searchIcon.png";
        notificationIconPath = app.getReletivePath() + File.separator + "public" + File.separator + "notificationIcon.png";
        profileIconPath = app.getReletivePath() + File.separator + "public" + File.separator + "profileIcon.png";
        
        // create the menu button
        JButton menuButton = ButtonFactory.createTransparentButton(dotIconPath);
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(menuButton, gbc);

        // create the home button
        JButton homeIconButton = ButtonFactory.createTransparentButton(logoPath);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.5;
        this.add(homeIconButton, gbc);

        // create the search textfield button
        JTextField searchField = new JTextField(20);
        searchField.setActionCommand("navBarSrch");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        this.add(searchField, gbc);

        // create the submit search button
        JButton searchSubmitButton = ButtonFactory.createTransparentButton(searchIconPath);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        this.add(searchSubmitButton, gbc);

        // create the notification button
        JButton notificationButton = ButtonFactory.createTransparentButton(notificationIconPath);
        gbc.gridx = 4;
        gbc.gridy = 0;
        this.add(notificationButton, gbc);

        // create the profile button
        JButton profileButton = ButtonFactory.createTransparentButton(profileIconPath);
        gbc.gridx = 5;
        gbc.gridy = 0;
        this.add(profileButton, gbc);
        
        
    }

    public void setMenuIcon(JButton menuButton, String imagePath){
        menuButton.setIcon(new ImageIcon(imagePath));
    }
}
