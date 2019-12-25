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
    private static String userName;
    private static User currentUser;

    private UserDB userDatabase;
    private MatchDB matchmakingDatabase;

    private static ArrayList<User> activeUsersList;
    private static ArrayAdapter<User> activeUserListAdapter;

    private static DatabaseReference databaseReference;
    private static FirebaseDatabase firebaseDatabase;

    private static AlertDialog activePlayersDialog, requestDialog;
    private static Match currentMatch;

    private static boolean startingGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = Services.getFirebaseDatabase();
        databaseReference = firebaseDatabase.getReference("");

        //
        userDatabase = new UserDB(firebaseDatabase);
        matchmakingDatabase = new MatchDB(firebaseDatabase);

        initializeDataBranches();
        initializeActivePlayersDialog();

        currentUser = new User();

        userName = this.getIntent().getStringExtra("username");
        //add the user to the database

        final Runner addUser = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                currentUser = valuePair.getUser();
                Services.setCurrentUser(currentUser);
                userDatabase.setUserActive(valuePair.getUser());

                setActiveUsersListener();
            }
        };
        userDatabase.addUser(userName,addUser);

        activeUsersList = new ArrayList<>();
        tictactoeB = findViewById(R.id.run_sim);

        //---------------New methods (Runner) ---------------------------------------
        tictactoeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.run_sim) {
                    activePlayersDialog.show();
                }
            }
        });

        initializeRequestDialog();
        setMatchmakingListener();
    }

    public void setUserInactive(){
        userDatabase.setUserInactive(currentUser);
        if(currentMatch!=null && currentMatch.getMatchKey()!=null && currentMatch.getMatchKey().compareTo("")!=0){
            databaseReference.child("matchmaking").child(currentMatch.getMatchKey()).removeValue();
        }
    }

    public void setActiveUsersListener(){
        final Runner getActiveUsers = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                activeUserListAdapter.clear();
                activeUserListAdapter.addAll(valuePair.getList());
                activeUserListAdapter.notifyDataSetChanged();
            }
        };
        userDatabase.setActiveUsersListener(getActiveUsers);
    }

    public void setMatchmakingListener(){
        final Runner onRequestReceived = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                Match m = valuePair.getMatch();
                if (m.getUser2().getUsername().compareTo(userName) == 0) {
                    currentMatch = valuePair.getMatch();
                    requestDialog.setTitle(valuePair.getString());
                    requestDialog.show();
                }
            }
        };
        matchmakingDatabase.setMatchListener(onRequestReceived);
    }

    public void initializeActivePlayersDialog(){
        ArrayList<User> temporaryList = new ArrayList<>();
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
                currentMatch = new Match(currentUser,selected);
                databaseReference.child("matchmaking").child(currentMatch.getMatchKey()).setValue(currentMatch);

                final Runner onRequestChange = new Runner() {
                    @Override
                    public void run(ValuePair valuePair) {
                        if(valuePair.getMatch().isAccepted()) {
                            matchmakingDatabase.getDBReference("requestSent").removeEventListener(matchmakingDatabase.getListener("requestSent"));

                            startingGame = true;

                            Intent i = new Intent(MainActivity.this, TicTacToeActivity.class);
                            i.putExtra("match", currentMatch.getMatchKey());
                            startActivity(i);
                        }
                    }
                };
                matchmakingDatabase.onRequestStateListener("requestSent",databaseReference.child("matchmaking").child(currentMatch.getMatchKey()), onRequestChange);
            }
        });

        title.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.refresh){

                    //on refresh retrieve the values from DB again
                     final Runner getActiveUsers = new Runner() {
                        @Override
                        public void run(ValuePair valuePair) {
                            activeUserListAdapter.clear();
                            activeUserListAdapter.addAll(valuePair.getList());
                            activeUserListAdapter.notifyDataSetChanged();
                        }
                    };
                    userDatabase.getActiveUsers(getActiveUsers);
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

        activePlayersDialog = builder.create();
    }

    public void initializeRequestDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentMatch.setAccepted(true);
                        matchmakingDatabase.updateGameState(currentMatch);

                        //request accepted
                        startingGame = true;

                        Intent i = new Intent(MainActivity.this, TicTacToeActivity.class);
                        i.putExtra("match", currentMatch.getMatchKey());
                        startActivity(i);
                    }
                }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        requestDialog = builder.create();
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
       if(!startingGame) setUserInactive();
        super.onStop();
    }

    public void initializeDataBranches(){
        User admin = new User("admin",0);
        databaseReference.child("users").child("active").child("admin").setValue(admin);

        databaseReference.child("users").child("all").child("admin").setValue(admin);

        String temp = "match_"+0+"_"+0;
        Match match = new Match(admin,admin);
        databaseReference.child("matchmaking").child(temp).setValue(match);


    }
}
