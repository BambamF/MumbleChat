package com.mumble.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * A ChatClientConnection provides methods to send messages to a live chat while preserving thread safety
 */
public class ChatClientConnection implements Runnable{
    private Socket socket;
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Initialises the ChatClientConnection with a host name and port number, also takes in the chat windows view panel
     * and associated scroll pane.
     * @param host the host name as a String
     * @param port the port number as an int
     * @param vp the associated view panel as a JPanel
     * @param sp the associated scroll pane as a JScrollPane
     * @throws IOException throws an IOException if socket connection fails
     */
    public ChatClientConnection(String host, int port, JPanel vp, JScrollPane sp) throws IOException{
        
        // initialise the socket with the host and the port, along with the view and scroll panels
        this.socket = new Socket(host, port);
        this.viewPanel = vp;
        this.scrollPane = sp;

        
        // initialise the out field with the socket output stream, using a print writer
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // initialise the in field with the socket input stream in a stream reader, using a buffered reader
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // run the object in a new thread
        new Thread(this).start();
    }

    /**
     * Starts the chat client connection
     */
    @Override
    public void run(){

        String line;

        try{

            // read entries to the socket via the inputstream
            while((line = in.readLine()) != null){

                String[] parts = line.split("::");

                // make sure the entry is in the right format
                if(parts.length >= 3){

                    String username = parts[0];
                    int avatarId = Integer.parseInt(parts[1]);
                    String message = parts[2];
                    Message finalMessage = new Message(0, username, avatarId, "00:00:00", message);

                    // renders the message to the screen
                    SwingUtilities.invokeLater(() -> {
                        sendMessage(finalMessage, viewPanel);
                    });

                }
                else{
                    throw new IllegalArgumentException("ChatClientConnection: Message parts length cannot be less than 3");
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Sends the message to the server via the output stream
     * @param username the username as a String
     * @param aId the avatar ID as an int
     * @param message the message as a String
     */
    public void send(String username, int aId, String message){
        String finalMessage = username + "::" + aId + "::" + message;
        out.println(finalMessage);
        out.flush();
    }

    /**
     * Renders the message to the screen via a chat bubble
     * @param message the message as a String
     * @param viewPanel the panel to be rendered in as a JPanel
     */
    public void sendMessage(Message message, JPanel viewPanel) {
        Random random = new Random();
        int rand = random.nextInt(2);
        Color colour = (rand == 1) ? ChatBubble.GREEN_BUBBLE : ChatBubble.BLUE_BUBBLE;
        ChatBubble bubble = new ChatBubble(colour, MumbleApp.COLUMN_WIDTH, "username", rand);
        bubble.addMessage(message);
    
        JPanel wrapper = new JPanel(new FlowLayout(rand == 1 ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 5));
        wrapper.setOpaque(false);
        wrapper.setBackground(new Color(0,0,0,0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(bubble);

        // Add the new message at the end of the viewPanel
        viewPanel.add(wrapper);
    
        // Revalidate and repaint the viewPanel to update the layout
        viewPanel.revalidate();
        viewPanel.repaint();
    
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
}
