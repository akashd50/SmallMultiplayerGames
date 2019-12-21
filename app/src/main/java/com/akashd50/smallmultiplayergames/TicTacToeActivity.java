package com.akashd50.smallmultiplayergames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TicTacToeActivity extends AppCompatActivity {
    private User user1, user2;
    private Match currentMatch;
    private static DatabaseReference databaseReference;
    private static FirebaseDatabase firebaseDatabase;

    private Button b0,b1,b2,b3,b4,b5,b6,b7,b8;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tictactoe_layout);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("");

        Intent i = getIntent();
        final String matchId = i.getStringExtra("match");

        final DatabaseReference dr = firebaseDatabase.getReference("users/all");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean matchFound = false;
                Match match= null;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    match = userSnapshot.getValue(Match.class);
                    if (matchId.compareTo(match.getMatchKey()) == 0) {
                        matchFound = true;
                        currentMatch = match;
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });



    }



    public void setListeners(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.box0:

                        break;
                    case R.id.box1:

                        break;
                    case R.id.box2:

                        break;
                    case R.id.box3:

                        break;
                    case R.id.box4:

                        break;
                    case R.id.box5:

                        break;
                    case R.id.box6:

                        break;
                    case R.id.box7:

                        break;
                    case R.id.box8:

                        break;
                }
            }
        };
        b0.setOnClickListener(listener);
        b1.setOnClickListener(listener);
        b2.setOnClickListener(listener);
        b3.setOnClickListener(listener);
        b4.setOnClickListener(listener);
        b5.setOnClickListener(listener);
        b6.setOnClickListener(listener);
        b7.setOnClickListener(listener);
        b8.setOnClickListener(listener);
    }

}
