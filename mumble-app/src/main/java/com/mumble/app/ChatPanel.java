package com.mumble.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.*;

/**
 * A Chat Panel provides methods to send messages, save messages and render chat bubbles on to the screen
 */
public class ChatPanel extends JPanel {

    public static final Color GREEN_BUBBLE = new Color(95, 252, 123);
    public static final Color BLUE_BUBBLE = new Color(0, 120, 254);
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private ChatClientConnection clientConn;
    private User user;
    private MumbleApp app;
    private int chatId;
    
    /**
     * Initialises and renders the chat panel 
     * @param a the MumbleApp object with the main thread
     */
    public ChatPanel(MumbleApp a, int cId){
        super(new BorderLayout());

        this.app = a;
        this.chatId = cId;

        // create the view panel
        viewPanel = new ScrollablePanel();
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        viewPanel.setBackground(Color.WHITE);

        // make the view panel scrollable
        scrollPane = new JScrollPane(viewPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  

        // Ensure the scrollPane fills space
        scrollPane.setViewportView(viewPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        this.user = app.getUser();
        System.out.println("ChatPanel user:" + this.user.getUsername());
        app.connectToServer(viewPanel, scrollPane, this.user);

        // set the client connection
        this.clientConn = app.getClientConn();

        System.out.println("ChatPanel connection: " + this.clientConn.toString());

        // get the chat history
        List<Message> messageHistory = DatabaseManager.getAllMessages();

        messageHistory.stream()
                        .forEach((message) -> {
                            clientConn.renderMessage(message);
                        });

        // create the input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // create a message field
        JTextField textField = new JTextField(30);
        textField.setActionCommand("jtf1");

        // set the action listener
        textField.addActionListener((ae) -> {
            String messageText = textField.getText().trim();

            // sanitise the input
            messageText = InputSanitiser.sanitniseHtml(messageText);

            System.out.println(messageText);

            // ensure the message exists and the server is connected
            if(!messageText.isEmpty() && clientConn != null){
                    try{

                        String username = this.user.getUsername();
            
                        int avatarId = 2; // or load based on user

                        Date date = new Date();
                        String timestamp = date.toString();

                        PrivateKey privateKey = CryptoUtils.loadPrivateKey(username);

                        byte[] signatureBytes = CryptoUtils.signMessage(messageText, privateKey);

                        String recipientUsername = ;

                        Message message = new Message(username, recipientUsername, avatarId, timestamp, messageText, signatureBytes, this.user);
                        clientConn.send(username, avatarId, messageText);
                        System.out.println("chat panel message: " + messageText);
                        DatabaseHelper.saveMessage(user.getUserId(), username, messageText, timestamp);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        app.reconnectToServer(viewPanel, scrollPane, user);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        app.reconnectToServer(viewPanel, scrollPane, user);
                    }    
            }   
            else if(clientConn == null){
                System.err.println("client conn is null");
            }
            else{
                System.err.println("textbox is empty");
            }
        });

        // create the send button
        JButton sendButton = new JButton("send");

        sendButton.addActionListener((ae) -> {
            String messageText = textField.getText().trim();

            // sanitise the input
            messageText = InputSanitiser.sanitniseHtml(messageText);

            System.out.println(messageText);

            // ensure the message exists and the server is connected
            if(!messageText.isEmpty() && clientConn != null){
                    try{

                        String username = this.user.getUsername();
            
                        int avatarId = 2; // or load based on user

                        Date date = new Date();
                        String timestamp = date.toString();

                        Message message = new Message(username, avatarId, timestamp, messageText);
                        clientConn.send(username, avatarId, messageText);
                        System.out.println("chat panel message: " + messageText);
                        DatabaseHelper.saveMessage(user.getUserId(), username, messageText, timestamp);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        app.reconnectToServer(viewPanel, scrollPane, user);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        app.reconnectToServer(viewPanel, scrollPane, user);
                    }    
            }     
            else if(clientConn == null){
                System.err.println("client conn is null");
            }
            else{
                System.err.println("textbox is empty");
            }
        });

        inputPanel.add(textField);
        inputPanel.add(sendButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) this.getComponent(0)).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        
    }

    /**
     * Adds a label to the chat panel to notify the user
     * @param message the notification message as a String
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    private void showMessage(String message, JPanel viewPanel, JScrollPane scrollPane){

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
            JScrollBar vertical = ((JScrollPane) this.getComponent(0)).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public JPanel getViewPanel(){
        return this.viewPanel;
    }

    public JScrollPane getScrollPane(){
        return this.scrollPane;
    }

}
