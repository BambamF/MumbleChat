package com.mumble.app;

public class User {
    
    private String username;
    private int avatarId;
    private int userId;
    
    public User(String uname, int aId, int uId){
        this.username = uname;
        this.avatarId = aId;
    }

    public String getUsername(){
        return this.username;
    }

    public void setAvatarId(int aId){
        this.avatarId = aId;
    }

    public int getAvatarId(){
        return this.avatarId;
    }

    public int getUserId(){
        return this.userId;
    }
}
