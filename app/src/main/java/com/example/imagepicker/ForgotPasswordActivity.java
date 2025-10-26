package com.example.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagepicker.db.DBHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Spinner securityQuestionSpinner;
    private EditText securityAnswerInput;
    private Button submitButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DBHelper(this);

        usernameInput = findViewById(R.id.username_input);
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        securityAnswerInput = findViewById(R.id.security_answer_input);
        submitButton = findViewById(R.id.submit_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String securityQuestion = securityQuestionSpinner.getSelectedItem().toString();
                String securityAnswer = securityAnswerInput.getText().toString();

                if (dbHelper.checkSecurityAnswer(username, securityQuestion, securityAnswer)) {
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
