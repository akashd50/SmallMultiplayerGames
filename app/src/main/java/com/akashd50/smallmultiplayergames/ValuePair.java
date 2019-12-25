package com.akashd50.smallmultiplayergames;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ValuePair {
    private String string;
    private int integer;
    private User user;
    private Match match;
    private ArrayList list;

    public ValuePair(){

    }

    public int getInteger() {
        return integer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public ArrayList getList() {
        return list;
    }

    public void setList(ArrayList list) {
        this.list = list;
    }

    public String getString() {
        return string;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public void setString(String string) {
        this.string = string;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
