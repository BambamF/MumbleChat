package com.mumble.app;

/**
 * A Message to creates a user message
 */
public class Message {
    private final int user_id;
    private final String username;
    private final int avatar_id;
    private final String timestamp;
    private final String message;

    /**
     * Instantiates the message
     * @param id the user ID as an int
     * @param uname the username as a String
     * @param aId the avatar ID as an int
     * @param time the timestamp as a String
     * @param text the user message as a String
     */
    public Message(int id, String uname, int aId, String time, String text){
        this.user_id = id;
        this.username = uname;
        this.avatar_id = aId;
        this.timestamp = time;
        this.message = text;
    }

    /**
     * Returns the user ID as an int
     * @return the user ID as an int
     */
    public int getUserId(){
        return this.user_id;
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
     * Returns the avatar ID as an int
     * @return the avatar ID as an int
     */      
    public int getAvatarId(){
        return this.avatar_id;
    }
}
