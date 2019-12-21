package com.akashd50.smallmultiplayergames;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        final EditText e = findViewById(R.id.usernameLogin);
        Button b = findViewById(R.id.log_in);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LogInActivity.this,MainActivity.class);
                System.out.println("Username; "+ e.getText().toString());
                i.putExtra("username", e.getText().toString());
                startActivity(i);
            }
        });

    }
}
