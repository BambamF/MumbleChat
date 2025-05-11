package com.mumble.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.Random;

import javax.swing.*;

/**
 * A Chat Panel provides methods to send messages, save messages and render chat bubbles on to the screen
 */
public class ChatPanel extends JPanel {

    private static final Color GREEN_BUBBLE = new Color(95, 252, 123);
    private static final Color BLUE_BUBBLE = new Color(0, 120, 254);
    private ChatClientConnection clientConn;
    
    /**
     * Initialises and renders the chat panel 
     * @param a the MumbleApp object with the main thread
     */
    public ChatPanel(MumbleApp a){
        super(new BorderLayout());

        // create the view panel
        JPanel viewPanel = new ScrollablePanel();
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        viewPanel.setBackground(Color.WHITE);

        // make the view panel scrollable
        JScrollPane scrollPane = new JScrollPane(viewPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  

        // Ensure the scrollPane fills space
        scrollPane.setViewportView(viewPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // connect to the server
        connectToServer(viewPanel, scrollPane);

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

            Message message = new Message(0,"username", 2, "00:00:00", messageText);

            // ensure the message exists and the server is connected
            if(!message.getMessage().isEmpty() && clientConn != null){
                if(clientConn != null){
                    try{
                        clientConn.send(message.getUsername(), message.getAvatarId(), messageText);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        reconnectToServer(viewPanel, scrollPane);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        reconnectToServer(viewPanel, scrollPane);
                    }    
                }
     
            }
            else{

                // notify the user that the connection is down
                showMessage("Chat not connected, retrying...", viewPanel, scrollPane);
                reconnectToServer(viewPanel, scrollPane);
            }

        });

        // create the send button
        JButton sendButton = new JButton("send");

        sendButton.addActionListener((ae) -> {
            String messageText = textField.getText();
            messageText = InputSanitiser.sanitniseHtml(messageText);

            Message message = new Message(0,"username", 2, "00:00:00", messageText);

            // ensure the message exists and the server is connected
            if(!message.getMessage().isEmpty() && clientConn != null){
                if(clientConn != null){
                    try{
                        clientConn.send(message.getUsername(), message.getAvatarId(), messageText);
                        textField.setText("");   
                    }      
                    catch(NullPointerException e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        reconnectToServer(viewPanel, scrollPane);
                    }      
                    catch(Exception e){
                        e.printStackTrace();
                        showMessage("Failed to send message. Reconnecting...", viewPanel, scrollPane);

                        // retry the connection
                        reconnectToServer(viewPanel, scrollPane);
                    }    
                }
     
            }
            else{

                // notify the user that the connection is down
                showMessage("Chat not connected, retrying...", viewPanel, scrollPane);
                reconnectToServer(viewPanel, scrollPane);
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
     * Activates the connection to the server via a ChatClientConnection
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    private void connectToServer(JPanel viewPanel, JScrollPane scrollPane){
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

    /**
     * Reconnect to the server after a failed connection attempt
     * @param viewPanel the view panel as a JPanel
     * @param scrollPane the scrollpane as a JScrollPane
     */
    private void reconnectToServer(JPanel viewPanel, JScrollPane scrollPane){
        Timer timer = new Timer(3000, e -> connectToServer(viewPanel, scrollPane) );
        timer.setRepeats(false);
        timer.start();
    }


}
