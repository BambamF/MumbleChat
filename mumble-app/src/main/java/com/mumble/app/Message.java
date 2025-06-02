package com.mumble.app;

/**
 * A Message to creates a user message
 */
public class Message {
    private final String username;
    private final String recepientUsername;
    private final int avatar_id;
    private final String timestamp;
    private final String message;
    private final byte[] messageSignatureBytes;
    private final User user;

    /**
     * Instantiates the message
     * @param id the user ID as an int
     * @param uname the username as a String
     * @param aId the avatar ID as an int
     * @param time the timestamp as a String
     * @param text the user message as a String
     */
    public Message(String uname, String recepUsername, int aId, String time, String messageText, byte[] signatureBytes, User u){
        this.username = uname;
        this.recepientUsername = recepUsername;
        this.avatar_id = aId;
        this.timestamp = time;
        this.message = messageText;
        this.messageSignatureBytes = signatureBytes;
        this.user = u;
    }

    /**
     * Returns the timestamp as a String
     * @return the timestamp as a String
     */    
    public String getTimestamp(){
        return this.timestamp;
    }

    /**
     * Returns the message as a String
     * @return the message as a String
     */      
    public String getMessage(){
        return this.message;
    }

    /**
     * Returns the username as a String
     * @return the username as a String
     */      
    public String getUsername(){
        return this.username;
    }

    /**
     * Returns the recepient username
     * @return the recepient username as a String
     */
    public String getRecepientUsername(){
        return this.recepientUsername;
    }

    /**
     * Returns the message signature bytes
     * @return
     */
    public byte[] getMessageSignatureBytes(){
        return this.messageSignatureBytes;
    }

    /**
     * Returns the message sender as a User object
     * @return the message sender as a User object
     */
    public User getMessageSender(){
        return this.user;
    }

    /**
     * Returns the avatar ID as an int
     * @return the avatar ID as an int
     */      
    public int getAvatarId(){
        return this.avatar_id;
    }
}
