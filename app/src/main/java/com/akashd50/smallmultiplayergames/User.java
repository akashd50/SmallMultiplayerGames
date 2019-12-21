package com.akashd50.smallmultiplayergames;

public class User {
    private String username;
    private int userid;
    private Match currentMatch;
    public User(){
        userid = 0;
    }

    public User(String username, int id){
        this.username = username;
        this.userid = id;
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    public void setCurrentMatch(Match currentMatch) {
        this.currentMatch = currentMatch;
    }

    public void copy(User u){
        this.username = u.username;
        this.userid = u.userid;
    }

    public void setUsername(String name){
        username = name;
    }

    public void setUserid(int id){
        userid = id;
    }

    public int getUserid(){return userid;}

    public String getUsername() {
        return username;
    }

    public String toString(){
        return this.username;
    }
}
