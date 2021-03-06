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

   /* public void search(final String toFind, final ValuePair toReturn, final Thread thread){
        allDR.addListenerForSingleValueEvent(new ValueEventListener() {
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
*/
    public void search(final String toFind, final Runner runner){
        final ValuePair toReturn = new ValuePair();
        allDR.addListenerForSingleValueEvent(new ValueEventListener() {
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
                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /*public void getAllUsers(final ArrayList<User> toReturn, final Thread thread){
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
    }*/

    public void getAllUsers(final Runner runner){
        allDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> activeUsers = new ArrayList<>();
                ValuePair toReturn = new ValuePair();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);


                    activeUsers.add(user);
                    //System.out.println(user);
                }

                toReturn.setList(activeUsers);
                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

   /* public void getActiveUsers(final ArrayList<User> toReturn, final Thread thread){
        activeDR.addListenerForSingleValueEvent(new ValueEventListener() {
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
    }*/

    public void getActiveUsers(final Runner runner){
        activeDR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> activeUsers = new ArrayList<>();
                ValuePair toReturn = new ValuePair();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);

                    if(user!=null) {
                        if (user.getUsername().compareTo(Services.getCurrentUser().getUsername()) != 0)
                            activeUsers.add(user);
                        //System.out.println(user);
                    }
                }
                toReturn.setList(activeUsers);


                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void setActiveUsersListener(final Runner runner){
        activeDR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> activeUsers = new ArrayList<>();
                ValuePair toReturn = new ValuePair();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if(user!=null && user.getUsername()!=null) {
                        if (user.getUsername().compareTo(Services.getCurrentUser().getUsername()) != 0)
                            activeUsers.add(user);
                        //System.out.println(user);
                    }
                }

                toReturn.setList(activeUsers);

                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }



    /*public void addUser(final String username, final User toAdd, final Thread thread){
        final ValuePair userSearch = new ValuePair();
        final ValuePair nextId = new ValuePair();

        final Thread addUser = new Thread(new Runnable() {
            @Override
            public void run() {
                toAdd.setUsername(username);
                toAdd.setUserid(nextId.getInteger());
                allDR.child(username).setValue(toAdd);
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
*/


    public void addUser(final String username, final Runner runner){
        final Runner addUser = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                User toAdd = new User(username, valuePair.getInteger());
                allDR.child(username).setValue(toAdd);

                valuePair.setUser(toAdd);

                runner.run(valuePair);
            }
        };


        Runner checkIfAlreadyExists = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                if(valuePair.getUser()==null){
                    getNextuserID(addUser);
                }else{
                    runner.run(valuePair);
                }
            }
        };
        search(username, checkIfAlreadyExists);
    }


    public void setUserActive(User u){
        activeDR.child(u.getUsername()).setValue(u);
    }

    public void setUserInactive(User u){
        activeDR.child(u.getUsername()).removeValue();
    }

   /* public ValuePair getNextuserID(final ValuePair valuePair, final Thread thread){
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
    }*/


    public void getNextuserID(final Runner runner){

        final DatabaseReference dr = firebaseDatabase.getReference("global_vars/player_ids");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ValuePair toReturn = new ValuePair();

                int val = dataSnapshot.getValue(Integer.class);
                toReturn.setInteger(val);
                dr.setValue(val+1);

                //run the passed thread
                runner.run(toReturn);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
