package com.mumble.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.*;

/**
 * A Chat Panel provides methods to send messages, save messages and render chat bubbles on to the screen
 */
public class ChatPanel extends JPanel {

    private static final Color GREEN_BUBBLE = new Color(95, 252, 123);
    private static final Color BLUE_BUBBLE = new Color(0, 120, 254);
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private ChatClientConnection clientConn;
    private User user;
    
    /**
     * Initialises and renders the chat panel 
     * @param a the MumbleApp object with the main thread
     */
    public ChatPanel(MumbleApp a, ChatClientConnection conn, User u){
        super(new BorderLayout());

        this.user = u;

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

        // set the client connection
        this.clientConn = conn;

        // connect to the server
        MumbleApp.connectToServer(viewPanel, scrollPane, user);

        // get the chat history
        List<Message> messageHistory = DatabaseManager.getAllMessages();

        messageHistory.stream()
                        .forEach((message) -> {
                            clientConn.sendMessage(message, viewPanel);
                        });

        // create the input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // create a message field
        JTextField textField = new JTextField(30);
        textField.setActionCommand("jtf1");

        // set the action listener
        textField.addActionListener((ae) -> {
            String messageText = textField.getText();

            // sanitise the input
            messageText = InputSanitiser.sanitniseHtml(messageText);

                                            // randomly change the user id based on message denom, CHANGE THIS!!!!!!
            Message message = new Message(messageText.length() % 2 == 0 ? 0 : 1,"username", 2, "00:00:00", messageText);

            // ensure the message exists and the server is connected
            if(!message.getMessage().isEmpty() && clientConn != null){
                if(clientConn != null){
                    try{
                        Date date = new Date();
                        String timestamp = date.toString();
                        clientConn.send(message.getUsername(), message.getAvatarId(), messageText);
                        DatabaseHelper.saveMessage(message.getUserId(), messageText, timestamp);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
                    }    
                }
     
            }
            else{

                // notify the user that the connection is down
                showMessage("Chat not connected, retrying...", viewPanel, scrollPane);
                MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
            }

        });

        // create the send button
        JButton sendButton = new JButton("send");

        sendButton.addActionListener((ae) -> {
            String messageText = textField.getText();

            // sanitise the input
            messageText = InputSanitiser.sanitniseHtml(messageText);

                                            // randomly change the user id based on message denom, CHANGE THIS!!!!!!
            Message message = new Message(messageText.length() % 2 == 0 ? 0 : 1,"username", 2, "00:00:00", messageText);

            // ensure the message exists and the server is connected
            if(!message.getMessage().isEmpty() && clientConn != null){
                if(clientConn != null){
                    try{
                        Date date = new Date();
                        String timestamp = date.toString();
                        clientConn.send(message.getUsername(), message.getAvatarId(), messageText);
                        DatabaseHelper.saveMessage(message.getUserId(), messageText, timestamp);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
                    }    
                }
     
            }
            else{

                // notify the user that the connection is down
                showMessage("Chat not connected, retrying...", viewPanel, scrollPane);
                MumbleApp.reconnectToServer(viewPanel, scrollPane, user);
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


}
