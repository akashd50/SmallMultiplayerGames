package com.akashd50.smallmultiplayergames;

import com.google.firebase.database.FirebaseDatabase;

public class Services {
    private static FirebaseDatabase firebaseDatabase;
    private static User currentUser;
    public static synchronized FirebaseDatabase getFirebaseDatabase(){
        if(firebaseDatabase==null) firebaseDatabase = FirebaseDatabase.getInstance();
        return firebaseDatabase;
    }

    public static synchronized User getCurrentUser(){
        return currentUser;
    }

    public static synchronized void setCurrentUser(User u){
        currentUser = u;
    }
}
