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

public class ChatClientConnection implements Runnable{
    private Socket socket;
    private JPanel viewPanel;
    private JScrollPane scrollPane;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClientConnection(String host, int port, JPanel vp, JScrollPane sp) throws IOException{
        this.socket = new Socket(host, port);
        this.viewPanel = vp;
        this.scrollPane = sp;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(this).start();
    }

    @Override
    public void run(){
        String line;
        try{
            while((line = in.readLine()) != null){
                String[] parts = line.split("::");
                if(parts.length >= 3){
                    String username = parts[0];
                    int avatarId = Integer.parseInt(parts[1]);
                    String message = parts[2];
                    Message finalMessage = new Message(0, username, avatarId, "00:00:00", message);
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

    public void send(String username, int aId, String message){
        String finalMessage = username + "::" + aId + "::" + message;
        out.println(finalMessage);
        out.flush();
    }

    private void sendMessage(Message message, JPanel viewPanel) {
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
