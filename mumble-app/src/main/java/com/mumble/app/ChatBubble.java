package com.mumble.app;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * This class provides methods to create a chat bubble and render it on the screen
 */
public class ChatBubble extends JPanel {

    public static final Color GREEN_BUBBLE = new Color(95, 252, 123);
    public static final Color BLUE_BUBBLE = new Color(0, 120, 254);

    private final Color backgroundColor;
    private final int arc;
    private final String username;
    private final int avatar_id;

    /**
     * Initialises the chat bubble with a given colour, arc, username and avatar Id
     * @param c the colour to which the bubble should be assigned as a Color object
     * @param a the arc to which the bubble should be assigned as an int
     * @param u the username as a String
     * @param aId the avatar ID as an int
     */
    public ChatBubble(Color c, int a, String u, int aId){
        this.backgroundColor = c;
        this.arc = a;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Top, left, bottom, right
        this.username = u;
        this.avatar_id = aId;
        int maxWidth = 300;
        setMaximumSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /**
     * Renders the chat bubble on the screen
     * @param g the graphics object to be used in the render
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(this.backgroundColor);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
    }

    /**
     * Returns the username as a String
     * @return the username as a String
     */
    private String getUsername(){
        return this.username;
    }

    /**
     * Returns the avatar ID as an int
     * @return the avatar ID as an int
     */
    private int getAvatarId(){
        return this.avatar_id;
    }

    /**
     * Adds the message bubble to the chat and render it on the screen
     * @param message the message to be rendered as a Message object
     */
    public void addMessage(Message message){
        int maxTextWidth = 280;
        JTextArea text = new JTextArea(message.getMessage());
        text.setForeground(Color.WHITE);
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false); // Make transparent
        text.setBorder(null);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.setMaximumSize(new Dimension(maxTextWidth, Integer.MAX_VALUE));
        textPanel.add(text, BorderLayout.CENTER);
        add(textPanel);
        revalidate();
        repaint();
    }
    
}
