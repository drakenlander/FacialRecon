package com.example.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagepicker.db.DBHelper;

public class ModifyCredentialsActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Spinner securityQuestionSpinner;
    private EditText securityAnswerInput;
    private Button submitButton;
    private DBHelper dbHelper;
    private String currentUsername;
    private String originalSecurityQuestion;
    private String originalSecurityAnswer;
    private int role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_credentials);

        dbHelper = new DBHelper(this);
        currentUsername = getIntent().getStringExtra("username");

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        securityAnswerInput = findViewById(R.id.security_answer_input);
        submitButton = findViewById(R.id.submit_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);

        loadUserData();

        securityQuestionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuestion = parent.getItemAtPosition(position).toString();
                if (selectedQuestion.equals(originalSecurityQuestion)) {
                    securityAnswerInput.setText(originalSecurityAnswer);
                } else {
                    securityAnswerInput.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });
    }

    private void loadUserData() {
        User user = dbHelper.getUser(currentUsername);
        if (user != null) {
            usernameInput.setText(user.getUsername());
            passwordInput.setText(user.getPassword());
            confirmPasswordInput.setText(user.getPassword());

            originalSecurityQuestion = user.getSecurityQuestion();
            originalSecurityAnswer = user.getSecurityAnswer();
            role = user.getRole();

            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) securityQuestionSpinner.getAdapter();
            int spinnerPosition = adapter.getPosition(user.getSecurityQuestion());
            securityQuestionSpinner.setSelection(spinnerPosition);

            securityAnswerInput.setText(user.getSecurityAnswer());
        }
    }

    private void updateUser() {
        String newUsername = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String securityQuestion = securityQuestionSpinner.getSelectedItem().toString();
        String securityAnswer = securityAnswerInput.getText().toString();

        if (password.equals(confirmPassword)) {
            dbHelper.updateUser(currentUsername, newUsername, password, securityQuestion, securityAnswer, role);
            Toast.makeText(ModifyCredentialsActivity.this, "Credentials updated successfully!", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("username", newUsername);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(ModifyCredentialsActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
    }
}
