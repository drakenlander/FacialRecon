package com.example.imagepicker;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.db.DBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ViewAttemptsActivity extends AppCompatActivity {

    private RecyclerView attemptsRecyclerView;
    private AttemptsAdapter attemptsAdapter;
    private DBHelper dbHelper;
    private List<Attempt> allAttempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attempts);

        dbHelper = new DBHelper(this);

        attemptsRecyclerView = findViewById(R.id.attempts_recycler_view);
        attemptsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allAttempts = dbHelper.getAllAttempts();

        attemptsAdapter = new AttemptsAdapter(allAttempts);
        attemptsRecyclerView.setAdapter(attemptsAdapter);

        FloatingActionButton exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> exportAttemptsToCSV());

        Spinner filterSpinner = findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAttempts(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void filterAttempts(String filter) {
        List<Attempt> filteredList;
        switch (filter) {
            case "Exitosos":
                filteredList = allAttempts.stream().filter(attempt -> attempt.getPersonId() != null).collect(Collectors.toList());
                break;
            case "Fallidos":
                filteredList = allAttempts.stream().filter(attempt -> attempt.getPersonId() == null).collect(Collectors.toList());
                break;
            default:
                filteredList = allAttempts;
                break;
        }
        attemptsAdapter.updateList(filteredList);
    }

    private void exportAttemptsToCSV() {
        List<Attempt> attempts = attemptsAdapter.getAttemptsList();
        if (attempts.isEmpty()) {
            Toast.makeText(this, "No attempts to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Person ID,Person Name,Timestamp\n");

        for (Attempt attempt : attempts) {
            csvContent.append(attempt.getId()).append(",");
            csvContent.append(attempt.getPersonId() != null ? attempt.getPersonId() : "N/A").append(",");
            csvContent.append(attempt.getPersonName()).append(",");
            csvContent.append(attempt.getTimestamp()).append("\n");
        }

        try {
            saveCsvToDownloads(this, csvContent.toString());
            Toast.makeText(this, "CSV exported to Downloads", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCsvToDownloads(Context context, String csvContent) throws IOException {
        String fileName = "attempts-" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(csvContent.getBytes());
                } else {
                    throw new IOException("Failed to get output stream.");
                }
            }
        } else {
            throw new IOException("Failed to create new MediaStore record.");
        }
    }
}
