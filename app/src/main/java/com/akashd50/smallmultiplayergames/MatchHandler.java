package com.akashd50.smallmultiplayergames;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MatchHandler {
    private Match match;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference matchmakingReference, communicationReference;
    public MatchHandler(Match m, FirebaseDatabase fd){
        this.match = m;
        this.firebaseDatabase = fd;
    }


}
