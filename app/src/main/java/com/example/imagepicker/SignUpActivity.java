package com.example.imagepicker;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagepicker.db.DBHelper;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Spinner securityQuestionSpinner;
    private EditText securityAnswerInput;
    private RadioGroup roleRadioGroup;
    private Button signUpButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dbHelper = new DBHelper(this);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        securityAnswerInput = findViewById(R.id.security_answer_input);
        roleRadioGroup = findViewById(R.id.role_radio_group);
        signUpButton = findViewById(R.id.sign_up_button);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();
                String securityQuestion = securityQuestionSpinner.getSelectedItem().toString();
                String securityAnswer = securityAnswerInput.getText().toString();

                int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedRoleId);
                int role = selectedRadioButton.getText().toString().equals("Role 1") ? 1 : 2;

                if (password.equals(confirmPassword)) {
                    dbHelper.insertUser(username, password, securityQuestion, securityAnswer, role);
                    Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
