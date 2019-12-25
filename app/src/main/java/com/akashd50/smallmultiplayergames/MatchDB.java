package com.akashd50.smallmultiplayergames;

import android.content.ContentValues;
import android.renderscript.Sampler;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchDB {
    private static String USER = "user";
    private static String ID = "id";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference matchmaking, v2;

    private HashMap<String, ValueEventListener> listeners;
    private HashMap<String, DatabaseReference> databaseReferences;

    public MatchDB(FirebaseDatabase fd){
        this.firebaseDatabase = fd;
        matchmaking = firebaseDatabase.getReference("matchmaking");
        v2 = firebaseDatabase.getReference("users/active");

        listeners = new HashMap<>();
        databaseReferences = new HashMap<>();
    }

    public void search(final String toFind, final Runner runner){
        matchmaking.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Match match = null;
                ValuePair toReturn = new ValuePair();

                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    match = userSnapshot.getValue(Match.class);

                    if(match!=null){

                        System.out.println("Match String (in search) -->>>> "+ match.getMatchKey() + " __ "+ toFind);

                        if(match.getMatchKey().compareTo(toFind)==0) {
                            System.out.println("_____>>>>> matched");
                            toReturn.setMatch(match);
                            break;
                        }
                    }
                }
                //run the provided action thread.
                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void onRequestStateListener(String key, DatabaseReference dr, final Runner runner){
        databaseReferences.put(key, dr);

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Match match = dataSnapshot.getValue(Match.class);
                if(match!=null && match.getUser1()!=null && match.getUser2()!=null) {
                    ValuePair vp = new ValuePair();
                    vp.setMatch(match);
                    runner.run(vp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        listeners.put(key,vel);
        dr.addValueEventListener(vel);
    }

    public ValueEventListener getListener(String key){
        return listeners.get(key);
    }

    public DatabaseReference getDBReference(String key){
        return databaseReferences.get(key);
    }

    public void setMatchListener(final Runner runner){
        matchmaking.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Match match = null;
                final ValuePair toReturn = new ValuePair();
                match = dataSnapshot.getValue(Match.class);
                if(match!=null && match.getUser1()!=null && match.getUser2()!=null) {
                    toReturn.setString("New Request From "+ match.getUser1().getUsername());
                    toReturn.setMatch(match);
                    runner.run(toReturn);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void updateGameState(Match match){
        matchmaking.child(match.getMatchKey()).setValue(match);
    }

    public ValuePair getNextuserID(final ValuePair valuePair, final Thread thread){
        final DatabaseReference dr = firebaseDatabase.getReference("global_vars/player_ids");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int val = dataSnapshot.getValue(Integer.class);
                valuePair.setInteger(val);
                dr.setValue(val+1);

                //run the passed thread
                thread.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        return valuePair;
    }
}
