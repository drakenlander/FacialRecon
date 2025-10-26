package com.example.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainMenuActivity extends AppCompatActivity {

    private static final int MODIFY_CREDENTIALS_REQUEST = 1;
    private String username;
    private int role;
    private TextView welcomeText;
    private Button adminButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get the username and role from the Intent
        username = getIntent().getStringExtra("username");
        role = getIntent().getIntExtra("role", -1);

        // Set the welcome text
        welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("Hi, " + username + "! (Role: " + role + ")");

        adminButton = findViewById(R.id.admin_button);
        signUpButton = findViewById(R.id.sign_up_button);

        // Set up the UI based on the user's role
        setupUIByRole();

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

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

        CardView modifyCredentialsCard = findViewById(R.id.modify_credentials_card);
        modifyCredentialsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ModifyCredentialsActivity.class);
                intent.putExtra("username", username);
                startActivityForResult(intent, MODIFY_CREDENTIALS_REQUEST);
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

    private void setupUIByRole() {
        switch (role) {
            case 1:
                adminButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.GONE);
                break;
            case 2:
                adminButton.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.VISIBLE);
                break;
            default:
                adminButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MODIFY_CREDENTIALS_REQUEST && resultCode == RESULT_OK && data != null) {
            username = data.getStringExtra("username");
            welcomeText.setText("Hi, " + username + "! (Role: " + role + ")");
        }
    }

    @Override
    public void onBackPressed() {
        // By leaving this method empty, we disable the back button.
        // The user must use the Log Out button to exit the main menu.
    }
}
