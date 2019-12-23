package com.akashd50.smallmultiplayergames;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MatchDB {
    private static String USER = "user";
    private static String ID = "id";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference matchmaking, v2;

    public MatchDB(FirebaseDatabase fd){
        this.firebaseDatabase = fd;
        matchmaking = firebaseDatabase.getReference("matchmaking");
        v2 = firebaseDatabase.getReference("users/active");

    }

    public void search(final String toFind, final ValuePair toReturn, final Thread thread){
        matchmaking.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = null;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    user = userSnapshot.getValue(User.class);
                    System.out.println("matching: "+ user.getUsername() + " <--> "+ toFind);
                    if(user.getUsername().compareTo(toFind)==0){
                        System.out.println("_____>>>>> matched");
                        toReturn.setUser(user);

                        break;
                    }
                }
                //run the provided action thread.
                thread.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


    public void addUser(final String username, final User toAdd, final Thread thread){
        final ValuePair userSearch = new ValuePair();
        final ValuePair nextId = new ValuePair();

        final Thread addUser = new Thread(new Runnable() {
            @Override
            public void run() {
                toAdd.setUsername(username);
                toAdd.setUserid(nextId.getInteger());
                matchmaking.child(username).setValue(toAdd);
                thread.run();
            }
        });


        Thread checkIfAlreadyExists = new Thread(new Runnable() {
            @Override
            public void run() {
                if(userSearch.getUser()==null){
                    getNextuserID(nextId,addUser);
                }else{
                    toAdd.copy(userSearch.getUser());
                    thread.run();
                }
                //System.out.println("-----------------------------------------------------> "+users);
            }
        });
        search(username, userSearch, checkIfAlreadyExists);
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
