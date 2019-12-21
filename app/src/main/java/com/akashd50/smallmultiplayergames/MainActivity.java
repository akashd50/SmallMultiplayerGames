package com.akashd50.smallmultiplayergames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button tictactoeB;
    private static int userID;
    private static String userName;
    private ActionBar actionBar;
    private static User currentUser;

    private UserDB userDatabase;


    private static ArrayList<User> activeUsersList;
    private static ArrayAdapter<User> activeUserListAdapter;

    private static DatabaseReference databaseReference;
    private static FirebaseDatabase firebaseDatabase;

    private static String ACTIVE_MATCHMAKING_STRING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("");

        //
        userDatabase = new UserDB(firebaseDatabase);

        initializeDataBranches();

        actionBar = getActionBar();
        currentUser = new User();

        userName = this.getIntent().getStringExtra("username");
        //checkOrAddUser();
        //add the user to the database
        currentUser = new User();
        Thread newT = new Thread(new Runnable() {
            @Override
            public void run() {
                userDatabase.setUserActive(currentUser);
            }
        });
        userDatabase.addUser(userName,currentUser, newT);


        activeUsersList = new ArrayList<>();
        tictactoeB = findViewById(R.id.run_sim);
        tictactoeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.run_sim) {
                    activeUsersList = new ArrayList<>();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            findAvailablePlayers();
                        }
                    }).start();

                    showDialog(activeUsersList);
                }
            }
        });

        //final ValuePair vp = new ValuePair();
        /*final ArrayList<User> users = new ArrayList<>();
        Thread newT = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("-----------------------------------------------------> "+users);
            }
        });*/
        //userDatabase.getNextuserID(vp,newT).getInteger();
        //userDatabase.getAllUsers(users, newT);


    }

    public void checkOrAddUser(){
        final DatabaseReference dr = firebaseDatabase.getReference("users/all");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;
                User user = null;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    user = userSnapshot.getValue(User.class);
                    if(user.getUsername().compareTo(userName)==0){
                        userFound = true;
                        break;
                    }
                }

                if(!userFound){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getNextuserID();
                        }
                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (currentUser) {
                                if (currentUser.getUserid() == 0) {
                                    try {
                                        currentUser.wait();
                                    } catch (InterruptedException e) {

                                    }
                                }
                            }
                            currentUser.setUsername(userName);
                            dr.child(userName).setValue(currentUser);

                            setUserActive();
                        }
                    }).start();
                }else{
                    currentUser.setUserid(user.getUserid());
                    currentUser.setUsername(user.getUsername());
                    setUserActive();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    
    public void setUserActive(){
        databaseReference.child("users").child("active").child(userName).setValue(currentUser);
    }

    public void setUserInactive(){
        databaseReference.child("users").child("active").child(userName).removeValue();
        if(ACTIVE_MATCHMAKING_STRING!=null && ACTIVE_MATCHMAKING_STRING.compareTo("")!=0){
            databaseReference.child("matchmaking").child(ACTIVE_MATCHMAKING_STRING).removeValue();
        }
    }
    
    public void getNextuserID(){
        DatabaseReference dr = firebaseDatabase.getReference("global_vars/player_ids");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int val = dataSnapshot.getValue(Integer.class);
                userID = val;
                synchronized (currentUser) {
                    currentUser.setUserid(val);
                    currentUser.notify();
                }

                System.out.println("Key: "+ userID +" : "+val);
                databaseReference.child("global_vars").child("player_ids").setValue(userID+1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void showDialog(ArrayList<User> temporaryList){

        final LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View v = inflater.inflate(R.layout.matching_dialog, null);

        Toolbar title = v.findViewById(R.id.activeUsersToolbar);
        title.setTitle("Active Users");
        title.inflateMenu(R.menu.sample);



        final ListView list = (ListView)v.findViewById(R.id.availablePlayersList);

        activeUserListAdapter = new ArrayAdapter<User>(this,R.layout.available_players_item,
                R.id.player_item,temporaryList);
        list.setAdapter(activeUserListAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selected = activeUserListAdapter.getItem(position);

                //matchmaking
                ACTIVE_MATCHMAKING_STRING = "match_"+currentUser.getUserid()+"_"+selected.getUserid();
                databaseReference.child("matchmaking").child(ACTIVE_MATCHMAKING_STRING).setValue(new Match(currentUser,selected));


            }
        });

        title.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //System.out.println("Refreshed....");
                if(item.getItemId()==R.id.refresh){
                    System.out.println("Refreshed....");
                    ArrayList<User> temp = new ArrayList<>(activeUsersList);

                    activeUserListAdapter.clear();
                    activeUserListAdapter.addAll(temp);
                    activeUserListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(v).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.create().show();
    }


    public void findAvailablePlayers(){
        DatabaseReference dr = firebaseDatabase.getReference("users/active");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                synchronized (activeUsersList) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        activeUsersList.add(user);
                        //System.out.println(user);
                    }
                    activeUsersList.notify();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        for(User u: activeUsersList){
            System.out.println(u);
        }
    }

    @Override
    public void onBackPressed() {
        setUserInactive();
        super.onBackPressed();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        setUserInactive();
        super.onStop();
    }

    public void initializeDataBranches(){
        User admin = new User("admin",0);
        databaseReference.child("users").child("active").child("admin").setValue(admin);
        databaseReference.child("users").child("all").child("admin").setValue(admin);
        String temp = "match_"+0+"_"+0;
        databaseReference.child("matchmaking").child(temp).setValue(new Match(admin, admin));
    }
}
