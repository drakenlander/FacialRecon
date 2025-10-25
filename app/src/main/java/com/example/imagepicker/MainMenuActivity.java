package com.example.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

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
    }
}
