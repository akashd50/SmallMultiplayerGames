package com.akashd50.smallmultiplayergames;

import com.google.firebase.database.FirebaseDatabase;

public class Services {
    private static FirebaseDatabase firebaseDatabase;

    public static synchronized FirebaseDatabase getFirebaseDatabase(){
        if(firebaseDatabase==null) firebaseDatabase = FirebaseDatabase.getInstance();
        return firebaseDatabase;
    }
}
