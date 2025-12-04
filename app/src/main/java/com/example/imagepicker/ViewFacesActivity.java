package com.example.imagepicker;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.db.DBHelper;
import com.example.imagepicker.face_recognition.FaceClassifier;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ViewFacesActivity extends AppCompatActivity {

    private RecyclerView facesRecyclerView;
    private FacesAdapter facesAdapter;
    private DBHelper dbHelper;
    private int role;
    private ArrayList<FaceClassifier.Recognition> allFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_faces);

        dbHelper = new DBHelper(this);
        role = getIntent().getIntExtra("role", -1);

        facesRecyclerView = findViewById(R.id.faces_recycler_view);
        facesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allFaces = new ArrayList<>(dbHelper.getAllFaces().values());

        facesAdapter = new FacesAdapter(this, allFaces, role);
        facesRecyclerView.setAdapter(facesAdapter);

        FloatingActionButton exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> exportFacesToCSV());

        SearchView searchView = findViewById(R.id.face_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String text) {
        ArrayList<FaceClassifier.Recognition> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(allFaces);
        } else {
            for (FaceClassifier.Recognition item : allFaces) {
                if (item.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                        String.valueOf(item.getCif()).contains(text) ||
                        item.getMajor().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        facesAdapter.updateList(filteredList);
    }

    private void exportFacesToCSV() {
        List<FaceClassifier.Recognition> faces = new ArrayList<>(dbHelper.getAllFaces().values());
        if (faces.isEmpty()) {
            Toast.makeText(this, "No faces to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Name,CIF,Major,Semester\n");

        for (FaceClassifier.Recognition face : faces) {
            csvContent.append(face.getId()).append(",");
            csvContent.append(face.getTitle()).append(",");
            csvContent.append(face.getCif()).append(",");
            csvContent.append(face.getMajor()).append(",");
            csvContent.append(face.getSemester()).append("\n");
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
        String fileName = "faces-" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".csv";

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
