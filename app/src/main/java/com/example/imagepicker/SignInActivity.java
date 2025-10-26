package com.example.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagepicker.db.DBHelper;

public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button signInButton;
    private TextView forgotPasswordText;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        dbHelper = new DBHelper(this);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        signInButton = findViewById(R.id.sign_in_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                try {
                    int role = dbHelper.checkUser(username, password);
                    if (role != -1) {
                        Intent intent = new Intent(SignInActivity.this, MainMenuActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("role", role);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("SignInActivity", "Sign-in failed", e);
                    Toast.makeText(SignInActivity.this, "An error occurred during sign-in.", Toast.LENGTH_LONG).show();
                }
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
