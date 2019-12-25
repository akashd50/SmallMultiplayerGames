package com.akashd50.smallmultiplayergames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TicTacToeActivity extends AppCompatActivity {
    private User user1, user2;
    private Match currentMatch;
    private ActiveGame activeGame;
    private User currentUser;
    private static DatabaseReference databaseReference;
    private static FirebaseDatabase firebaseDatabase;
    private MatchDB matchDatabase;
    private Button[] buttons;
    private Button rematchButton;
    //private boolean isMyTurn;
    private TextView messageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tictactoe_layout);

        messageView = findViewById(R.id.gameStatusView);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("");

        matchDatabase = new MatchDB(firebaseDatabase);

        Intent i = this.getIntent();
        final String matchId = i.getStringExtra("match");

        System.out.println("Match String -->>>> "+ matchId);

        currentUser = Services.getCurrentUser();

        final Runner searchMatch = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                currentMatch = valuePair.getMatch();
                user1 = currentMatch.getUser1();
                user2 = currentMatch.getUser2();
                activeGame = currentMatch.getActiveGame();
                setGameStateListener();

                if(user1.getUsername().compareTo(currentUser.getUsername()) == 0 &&
                        currentMatch.getActiveGame().getPlayerTurn() == 1){
                    //isMyTurn = true;
                    messageView.setText("Your Turn!");
                }else if(user2.getUsername().compareTo(currentUser.getUsername()) == 0 &&
                        currentMatch.getActiveGame().getPlayerTurn() == 2){
                    //isMyTurn = true;
                    messageView.setText("Your Turn!");
                }else{
                    //isMyTurn = false;
                    messageView.setText("Waiting for opponent!");
                }
            }
        };
        matchDatabase.search(matchId, searchMatch);


        setListeners();

    }



    public void setListeners(){
        buttons = new Button[9];

        buttons[0] = findViewById(R.id.box0);
        buttons[1] = findViewById(R.id.box1);
        buttons[2] = findViewById(R.id.box2);
        buttons[3] = findViewById(R.id.box3);
        buttons[4] = findViewById(R.id.box4);
        buttons[5] = findViewById(R.id.box5);
        buttons[6] = findViewById(R.id.box6);
        buttons[7] = findViewById(R.id.box7);
        buttons[8] = findViewById(R.id.box8);
        rematchButton = findViewById(R.id.rematchRequest);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.box0:
                        if(isMyTurn()) updateGameBoard(0,0, buttons[0]);
                        break;
                    case R.id.box1:
                        if(isMyTurn()) updateGameBoard(0,1, buttons[1]);

                        break;
                    case R.id.box2:
                        if(isMyTurn()) updateGameBoard(0,2, buttons[2]);

                        break;
                    case R.id.box3:
                        if(isMyTurn()) updateGameBoard(1,0, buttons[3]);

                        break;
                    case R.id.box4:
                        if(isMyTurn()) updateGameBoard(1,1, buttons[4]);

                        break;
                    case R.id.box5:
                        if(isMyTurn()) updateGameBoard(1,2, buttons[5]);
                        break;
                    case R.id.box6:
                        if(isMyTurn()) updateGameBoard(2,0, buttons[6]);

                        break;
                    case R.id.box7:
                        if(isMyTurn()) updateGameBoard(2,1, buttons[7]);

                        break;
                    case R.id.box8:
                        if(isMyTurn()) updateGameBoard(2,2, buttons[8]);

                        break;
                    case R.id.rematchRequest:
                        if(currentMatch!=null){
                            currentMatch.getActiveGame().resetBoard();
                            matchDatabase.updateGameState(currentMatch);
                        }

                        break;
                }
            }
        };
        buttons[0].setOnClickListener(listener);
        buttons[1].setOnClickListener(listener);
        buttons[2].setOnClickListener(listener);
        buttons[3].setOnClickListener(listener);
        buttons[4].setOnClickListener(listener);
        buttons[5].setOnClickListener(listener);
        buttons[6].setOnClickListener(listener);
        buttons[7].setOnClickListener(listener);
        buttons[8].setOnClickListener(listener);
        rematchButton.setOnClickListener(listener);
    }


    public void updateGameBoard(int row, int col, Button b){
        if(currentUser.getUsername().compareTo(user1.getUsername()) ==0 ) {
            activeGame.update(row, col, 0);
            b.setText("O");
        }
        else {
            activeGame.update(row, col, 1);
            b.setText("X");
        }

        if(user1.getUsername().compareTo(currentUser.getUsername()) == 0){
            currentMatch.getActiveGame().setTurn(2);
        }else if(user2.getUsername().compareTo(currentUser.getUsername()) == 0){
            currentMatch.getActiveGame().setTurn(1);
        }

        matchDatabase.updateGameState(currentMatch);
    }

    public boolean isMyTurn(){
        if(activeGame.isGameWon()==-1) {
            if (user1.getUsername().compareTo(currentUser.getUsername()) == 0 &&
                    currentMatch.getActiveGame().getPlayerTurn() == 1) {
                return true;
            } else if (user2.getUsername().compareTo(currentUser.getUsername()) == 0 &&
                    currentMatch.getActiveGame().getPlayerTurn() == 2) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void setGameStateListener(){
        final Runner gameStateChange = new Runner() {
            @Override
            public void run(ValuePair valuePair) {
                List l = valuePair.getMatch().getActiveGame().getGameBoard();

                currentMatch.getActiveGame().setGameBoard(valuePair.getMatch().getActiveGame().getGameBoard());
                currentMatch.getActiveGame().setTurn(valuePair.getMatch().getActiveGame().getPlayerTurn());

                for(int i=0;i<buttons.length;i++){
                    if(l.get(i).equals(1)){
                        buttons[i].setText("X");
                    }else if(l.get(i).equals(0)){
                        buttons[i].setText("O");
                    }else{
                        buttons[i].setText("");
                    }
                }

                int gameStatus = valuePair.getMatch().getActiveGame().isGameWon();
                if(gameStatus ==-1) {
                    if (isMyTurn()) {
                        messageView.setText("Your Turn!");
                    } else {
                        messageView.setText("Waiting for opponent!");
                    }
                }else{
                    if(gameStatus==0) {
                        messageView.setText(user1.getUsername() + " won the game!");
                    }else if(gameStatus ==1 ){
                        messageView.setText(user2.getUsername() + " won the game!");
                    }
                }
            }
        };
        matchDatabase.onRequestStateListener("gamestate", databaseReference.child("matchmaking").child(currentMatch.getMatchKey()),gameStateChange);
    }
}
