package com.akashd50.smallmultiplayergames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.firebase.database.ChildEventListener;
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
    private MatchDB matchmakingDatabase;

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
        matchmakingDatabase = new MatchDB(firebaseDatabase);

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
        userDatabase.addUser(userName, currentUser, newT);


        activeUsersList = new ArrayList<>();
        tictactoeB = findViewById(R.id.run_sim);
        tictactoeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.run_sim) {
                    activeUsersList = new ArrayList<>();
                    Thread newT = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            activeUserListAdapter.notifyDataSetChanged();
                        }
                    });
                    userDatabase.getActiveUsers(activeUsersList, newT);

                    showDialog(activeUsersList);
                }
            }
        });



        //--------------------------------------
        final Runner onRequestReceived = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                Match match = valuePair.getMatch();
                if (match.getUser2().getUsername().compareTo(userName) == 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(valuePair.getString()).setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        };

        matchmakingDatabase.setMatchListener(onRequestReceived);



    }

    public void setUserInactive(){
        userDatabase.setUserInactive(currentUser);

        if(ACTIVE_MATCHMAKING_STRING!=null && ACTIVE_MATCHMAKING_STRING.compareTo("")!=0){
            databaseReference.child("matchmaking").child(ACTIVE_MATCHMAKING_STRING).removeValue();
        }
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
