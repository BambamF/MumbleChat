package com.mumble.app.ClientServerConnection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.swing.*;

import com.mumble.app.Message;
import com.mumble.app.MumbleApp;
import com.mumble.app.User;
import com.mumble.app.Panels.ChatPanel;
import com.mumble.app.Utils.CryptoUtils;

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
    private List<User> connectedUsers;
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
        this.connectedUsers = new ArrayList<>();
        this.connectedUsers.add(u);
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
                if(parts.length > 3 && !parts[0].equals("LOGIN")){

                    // extract the parts of the message
                    String senderUsername = parts[0];
                    String recipientUsername = parts[1];
                    if (!recipientUsername.equals(user.getUsername())) {
                        continue; // Skip message not intended for this client
                    }
                    int avatarId = Integer.parseInt(parts[2]);
                    String message = parts[3];
                    byte[] messageSignature = Base64.getDecoder().decode(parts[4]);
                    String decryptedMessage;

                    try{

                        // load the private key
                        PrivateKey privateKey = CryptoUtils.loadPrivateKey(user.getUsername());

                        // decrypt the message
                        byte[] decryptedBytes = CryptoUtils.decryptMessage(message, privateKey);

                        // get the senders public key
                        PublicKey senderPublicKey = CryptoUtils.getPublicKeyByUsername(senderUsername);

                        // verify the encrypted message signature
                        boolean isVerified = CryptoUtils.verifySignature(message, messageSignature, senderPublicKey);

                        if(!isVerified){
                            System.err.println("Message verification failed: possible tampering.");
                            SwingUtilities.invokeLater(() -> {
                                MumbleApp.showMessage("Message could not be verified, possible tampering", viewPanel, scrollPane);
                            });
                            return; 
                        }

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

                    List<String> recepientUsernames = this.connectedUsers.stream()
                                                            .map(u -> u.getUsername())
                                                            .collect(Collectors.toList());

                    Message finalMessage = new Message(this.user.getUsername(), 
                                                        recepientUsernames, 
                                                        avatarId, 
                                                        timestamp, 
                                                        decryptedMessage, 
                                                        messageSignature, 
                                                        this.user);

                    System.out.println("CCC: run validation before render");
                    // renders the message to the screen
                    SwingUtilities.invokeLater(() -> {
                        renderMessage(finalMessage);
                    });

                }
                else if(parts[0].equals("LOGIN")){
                    return;
                }
                else{
                    throw new IllegalArgumentException("ChatClientConnection: Message parts length cannot be less than 5");
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            this.closeConnection();
        }
    }

    /**
     * Sends the message to the server via the output stream
     * @param recipientUsername the username as a String
     * @param aId the avatar ID as an int
     * @param message the message as a String
     */
    public void send(List<String> recipientUsernames, int aId, Message message){
        for(String recipientUsername : recipientUsernames){
           try{

            System.out.println("CCC: send method validation");

            PublicKey recipientKey = CryptoUtils.getPublicKeyByUsername(recipientUsername);

            PrivateKey privateKey = CryptoUtils.loadPrivateKey(user.getUsername());

            if(recipientKey == null){
                System.err.println("Public key not found for: " + recipientUsername);
                return;
            }

            String encryptedBase64 = CryptoUtils.encryptToBase64(message.getMessage(), recipientKey);

            byte[] messageSignature = CryptoUtils.signMessage(encryptedBase64, privateKey);

            String senderUsername = this.user.getUsername();

            String finalMessage =   senderUsername +
                                    "::" +
                                    recipientUsername + 
                                    "::" + 
                                    aId + 
                                    "::" + 
                                    encryptedBase64 + 
                                    "::" + 
                                    Base64.getEncoder().encodeToString(messageSignature);

            out.println(finalMessage);
            out.flush();
            
        }
        catch(Exception e){
            e.printStackTrace();
        } 
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
     * Gracefully closes the connection
     */
    public void closeConnection(){
        try{
            if(this.in != null){
                this.in.close();
            }
            if(this.out != null){
                this.out.close();
            }
            if(!this.socket.isClosed()){
                this.socket.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void renderMessage(Message message) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(app.getUser().getUsername().equals(message.getUsername()) ? ChatPanel.GREEN_BUBBLE : ChatPanel.BLUE_BUBBLE);
        bubble.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel usernameLabel = new JLabel(message.getUsername());
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        usernameLabel.setForeground(Color.WHITE);

        JLabel messageLabel = new JLabel("<html>" + message.getMessage() + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(Color.WHITE);

        bubble.add(usernameLabel);
        bubble.add(messageLabel);

        JLabel timestampLabel = new JLabel(message.getTimestamp());
        timestampLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
        timestampLabel.setForeground(Color.LIGHT_GRAY);

        JPanel timestampWrapper = new JPanel(new FlowLayout());
        timestampWrapper.add(timestampLabel, FlowLayout.LEFT);

        JPanel wrapper = new JPanel(); // RIGHT or LEFT based on sender
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.add(bubble);
        wrapper.add(timestampWrapper);

        wrapper.setAlignmentX(app.getUser().getUsername().equals(message.getUsername()) ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT); // or LEFT based on user


        viewPanel.add(wrapper);
        viewPanel.revalidate();
        viewPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Returns the list of connected users
     * @return the list of connected users as a List
     */
    public List<User> getConnectedUsers(){
        return this.connectedUsers;
    }
    
}
