package com.example.imagepicker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.db.DBHelper;
import com.example.imagepicker.face_recognition.FaceClassifier;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewFacesActivity extends AppCompatActivity {

    private RecyclerView facesRecyclerView;
    private FacesAdapter facesAdapter;
    private DBHelper dbHelper;
    private int role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_faces);

        dbHelper = new DBHelper(this);
        role = getIntent().getIntExtra("role", -1);

        facesRecyclerView = findViewById(R.id.faces_recycler_view);
        facesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // This is a placeholder. We will replace this with a real method to get faces as a list.
        HashMap<String, FaceClassifier.Recognition> facesMap = dbHelper.getAllFaces();
        ArrayList<FaceClassifier.Recognition> facesList = new ArrayList<>(facesMap.values());

        facesAdapter = new FacesAdapter(this, facesList, role);
        facesRecyclerView.setAdapter(facesAdapter);
    }
}
