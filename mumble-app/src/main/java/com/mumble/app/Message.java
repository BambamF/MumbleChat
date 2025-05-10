package com.mumble.app;

public class Message {
    private final int user_id;
    private final String username;
    private final int avatar_id;
    private final String timestamp;
    private final String message;

    public Message(int id, String uname, int aId, String time, String text){
        this.user_id = id;
        this.username = uname;
        this.avatar_id = aId;
        this.timestamp = time;
        this.message = text;
    }

    public int getUserId(){
        return this.user_id;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getMessage(){
        return this.message;
    }

    public String getUsername(){
        return this.username;
    }

    public int getAvatarId(){
        return this.avatar_id;
    }
}
