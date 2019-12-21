package com.akashd50.smallmultiplayergames;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserDB {
    private static String USER = "user";
    private static String ID = "id";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference allDR, activeDR;

    public UserDB(FirebaseDatabase fd){
        this.firebaseDatabase = fd;
        allDR = firebaseDatabase.getReference("users/all");
        activeDR = firebaseDatabase.getReference("users/active");
    }

    public void search(final String toFind, final ValuePair toReturn, final Thread thread){
        allDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = null;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    user = userSnapshot.getValue(User.class);
                    if(user.getUsername().compareTo(toFind)==0){
                        toReturn.setUser(user);

                        //run the provided action thread.
                        thread.run();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void getAllUsers(final ArrayList<User> toReturn, final Thread thread){
        allDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //synchronized (toReturn) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        toReturn.add(user);
                        //System.out.println(user);
                    }

                    thread.run();
                  //  toReturn.notify();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        for(User u: toReturn){
            System.out.println(u);
        }
    }


    public void addUser(final String username, final User toAdd, final Thread thread){
        final ValuePair userSearch = new ValuePair();
        final ValuePair nextId = new ValuePair();
        Thread newT = new Thread(new Runnable() {
            @Override
            public void run() {
                if(userSearch.getUser()==null){
                   if(nextId.getInteger()!=0){
                       toAdd.setUsername(username);
                       toAdd.setUserid(nextId.getInteger());

                       allDR.child(username).setValue(toAdd);
                       thread.run();
                   }
                }
                //System.out.println("-----------------------------------------------------> "+users);
            }
        });
        search(username, userSearch, newT);
        getNextuserID(nextId,newT);
    }

    public void setUserActive(User u){
        activeDR.child(u.getUsername()).setValue(u);
    }

    public void setUserInactive(User u){
        activeDR.child(u.getUsername()).removeValue();
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
