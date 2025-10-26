package com.example.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get the username from the Intent
        String username = getIntent().getStringExtra("username");

        // Set the welcome text
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("Hi, " + username + "!");

        CardView viewFacesCard = findViewById(R.id.view_faces_card);
        viewFacesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ViewFacesActivity.class);
                startActivity(intent);
            }
        });

        CardView viewAttemptsCard = findViewById(R.id.view_attempts_card);
        viewAttemptsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ViewAttemptsActivity.class);
                startActivity(intent);
            }
        });

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // By leaving this method empty, we disable the back button.
        // The user must use the Log Out button to exit the main menu.
    }
}
