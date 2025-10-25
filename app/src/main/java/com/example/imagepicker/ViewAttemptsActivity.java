package com.example.imagepicker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.db.DBHelper;

import java.util.List;

public class ViewAttemptsActivity extends AppCompatActivity {

    private RecyclerView attemptsRecyclerView;
    private AttemptsAdapter attemptsAdapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attempts);

        dbHelper = new DBHelper(this);

        attemptsRecyclerView = findViewById(R.id.attempts_recycler_view);
        attemptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Attempt> attempts = dbHelper.getAllAttempts();

        attemptsAdapter = new AttemptsAdapter(attempts);
        attemptsRecyclerView.setAdapter(attemptsAdapter);
    }
}
