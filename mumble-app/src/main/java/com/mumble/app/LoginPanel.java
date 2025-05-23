package com.mumble.app;

import javax.swing.*;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.awt.*;
import java.util.Arrays;


/**
 * A LoginPanel creates and renders a login page with fields for user input
 */
public class LoginPanel extends JPanel {
    
    private MumbleApp app;
    private ChatClientConnection chatConnection;

    /**
     * Instantiates and renders the login page
     * @param a the MumbleApp object that has the main thread
     */
    public LoginPanel(MumbleApp a) {

        this.app = a;
        setLayout(new BorderLayout());
    
        JTextField usernameField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);
    
        JButton loginButton = new JButton("login");
        JButton createAccountButton = new JButton("create account");
        createAccountButton.setBorderPainted(false);
        createAccountButton.setOpaque(false);
        createAccountButton.setBackground(Color.WHITE);
        createAccountButton.setToolTipText("create your account");
    
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
    
        gbc.insets = new Insets(10, 10, 10, 10); // spacing between components
        gbc.anchor = GridBagConstraints.EAST; // align labels to the right
        gbc.fill = GridBagConstraints.NONE;
    
        // Row 0: Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        fields.add(new JLabel("username: "), gbc);
    
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        fields.add(usernameField, gbc);
    
        // Row 1: Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        fields.add(new JLabel("password: "), gbc);
    
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        fields.add(pwField, gbc);
    
        // Row 2: Create account button (left), Login button (right)
        gbc.gridy = 2;
        gbc.gridx = 0;
        fields.add(createAccountButton, gbc);
    
        gbc.gridx = 1;
        fields.add(loginButton, gbc);
    
        add(fields, BorderLayout.CENTER);
    
        // Action listeners
        loginButton.addActionListener((ae) -> {
            String username = usernameField.getText();
            username = InputSanitiser.sanitiseUsername(username);
            char[] password = pwField.getPassword();
    
            int user_id = DatabaseManager.getUserId(username, new String(password));
            if(username.isEmpty()){
                JOptionPane.showMessageDialog(this, "please enter a username");
                if(password.length == 0){
                    JOptionPane.showMessageDialog(this, "please enter a password");
                }
                return;
            }

            String pwFromDb = DatabaseManager.getPassword(username);

                if(!DatabaseManager.usernameExists(username)){
                    JOptionPane.showMessageDialog(this, "username not found! please create an account");
                }
                else if(!BCrypt.checkpw(new String(password), pwFromDb)){
                    JOptionPane.showMessageDialog(this, "incorrect password, please try again");
                }
                else{
                    usernameField.setText("");
                    pwField.setText("");
                    int userId = DatabaseManager.getUserId(username, pwFromDb);
                    // send loginCode to the chatClientConnection, this sets the User object with the users username
                    app.setUser(username, userId);

                    // construct ChatPanel *after* setting the user
                    ChatPanel chatPanel = new ChatPanel(app);
                    app.setChatPanel(chatPanel);  // add a method in MumbleApp to store this
                    this.chatConnection = app.getClientConn();
                    this.chatConnection.send("LOGIN", username);
                    System.out.println("LoginPanel connection: " + this.chatConnection.toString());
                    System.out.println("LoginPanel user: " + app.getUser().getUsername());
                    app.showChatPage();
                }
            
            Arrays.fill(password, '0');


        });
    
        createAccountButton.addActionListener((ae) -> {
            MumbleApp.showCreateAccountPage();
        });
        
    }

}
