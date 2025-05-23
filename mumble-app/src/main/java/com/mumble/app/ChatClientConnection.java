package com.mumble.app;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.swing.*;

/**
 * A ChatClientConnection provides methods to send messages to a live chat while preserving thread safety
 */
public class ChatClientConnection implements Runnable{
    private Socket socket;
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private MumbleApp app;

    /**
     * Initialises the ChatClientConnection with a host name and port number, also takes in the chat windows view panel
     * and associated scroll pane.
     * @param host the host name as a String
     * @param port the port number as an int
     * @param vp the associated view panel as a JPanel
     * @param sp the associated scroll pane as a JScrollPane
     * @throws IOException throws an IOException if socket connection fails
     */
    public ChatClientConnection(String host, int port, JPanel vp, JScrollPane sp, User u, MumbleApp a) throws IOException{
        
        // initialise the socket with the host and the port, along with the view and scroll panels
        this.socket = new Socket(host, port);
        this.viewPanel = vp;
        this.scrollPane = sp;
        this.user = u;
        this.app = a;

        
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
                if(parts.length >= 3 && !parts[0].equals("LOGIN")){

                    System.out.println("CCC run:" + line);

                    // extract the parts of the message
                    String recipientUsername = parts[0];
                    if (!recipientUsername.equals(user.getUsername())) {
                        continue; // Skip message not intended for this client
                    }
                    int avatarId = Integer.parseInt(parts[1]);
                    String message = parts[2];
                    String decryptedMessage;

                    // decrypt the message 
                    try{

                        // load the private key
                        PrivateKey privateKey = CryptoUtils.loadPrivateKey(user.getUsername());

                        // decrypt the message
                        byte[] decryptedBytes = CryptoUtils.decryptMessage(message, privateKey);

                        // set the final message as a string
                        decryptedMessage = new String(decryptedBytes);

                        System.out.println("CCC dec msg:" + decryptedMessage);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        decryptedMessage = "Message decryption failed";
                    }

                    Date date = new Date();
                    String timestamp = date.toString();

                    System.out.println("CCC Decrypted msg: " + decryptedMessage);

                    Message finalMessage = new Message(recipientUsername, avatarId, timestamp, decryptedMessage);

                    // renders the message to the screen
                    SwingUtilities.invokeLater(() -> {
                        renderMessage(finalMessage);
                    });

                }
                else if(parts[0].equals("LOGIN")){
                    return;
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
    public void send(String recipientUsername, int aId, String message){
        try{

            PublicKey recipientKey = CryptoUtils.getPublicKeyByUsername(recipientUsername);

            if(recipientKey == null){
                System.err.println("Public key not found for: " + recipientUsername);
                return;
            }

            String encryptedBase64 = CryptoUtils.encryptToBase64(message, recipientKey);

            String finalMessage = recipientUsername + "::" + aId + "::" + encryptedBase64;
            out.println(finalMessage);
            out.flush();
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sends a login code for a user object
     * @param loginCode the login code as a String
     * @param username the username as a String
     */
    public void send(String loginCode, String username){
        String loginMessage = loginCode + "::" + username + "::" + "NO_MESSAGE";
        out.println(loginMessage);
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

        public void renderMessage(Message message) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(app.getUser().getUsername().equals(message.getUsername()) ? ChatPanel.GREEN_BUBBLE : ChatPanel.BLUE_BUBBLE);
        bubble.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel usernameLabel = new JLabel(message.getUsername());
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel messageLabel = new JLabel("<html>" + message.getMessage() + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        bubble.add(usernameLabel);
        bubble.add(messageLabel);

        bubble.setAlignmentX(app.getUser().getUsername().equals(message.getUsername()) ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT); // or LEFT based on user

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // RIGHT or LEFT based on sender
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        viewPanel.add(wrapper);
        viewPanel.revalidate();
        viewPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
}
