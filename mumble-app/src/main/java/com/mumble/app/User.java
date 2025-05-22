package com.mumble.app;

public class User {
    
    private String username;
    private int avatarId;
    
    public User(String uname, int aId){
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
}
