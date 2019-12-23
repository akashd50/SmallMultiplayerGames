package com.akashd50.smallmultiplayergames;

public class Match {
    private User user1, user2;
    private String matchKey;

    public Match(){

    }
    public Match(User u1, User u2){
        user1 = u1;user2 = u2;
        matchKey = "match_"+user1.getUserid()+"_"+user2.getUserid();
    }

    public String getMatchKey() {
        return matchKey;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }
}
